package com.zxq.sudoku

import android.Manifest
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionOptions
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionType
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.zxq.sudoku.games.tetris.MatchEvent
import com.zxq.sudoku.games.tetris.MatchTransport
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicBoolean

internal class AndroidNearbyMatchTransport(private val activity: MainActivity) : MatchTransport {
    override val available = true
    private val client by lazy { Nearby.getConnectionsClient(activity) }
    private val serviceId = "${activity.packageName}.tetris.match"
    private val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
    override val nfcAvailable: Boolean get() = nfcAdapter?.isEnabled == true &&
        activity.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)
    private var pairing = false
    private var connectionCodeMode = false
    private var ownNonce = ""
    private val peerNonces = mutableMapOf<String, String>()
    private val readerEnabled = AtomicBoolean(false)
    private var nfcPeerId: String? = null
    private var nfcSecret: ByteArray? = null
    @Volatile private var touchedReaderNonce: String? = null
    private var activeNfc = false
    private var pairingComplete = false
    private var incomingConnection = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var listener: (MatchEvent) -> Unit = {}
    @Volatile private var endpointId: String? = null
    private var pendingPermissionAction: (() -> Unit)? = null
    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.isNotEmpty() && grants.values.all { it }) pendingPermissionAction?.invoke()
        else emit(MatchEvent.Error(when {
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == false ->
                "好友对战需要精确位置权限，请选择「精确位置」并允许"
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == false ->
                "好友对战需要位置权限，请在系统设置中允许"
            else -> "建立对战需要附近设备权限，请在系统设置中允许"
        }))
        pendingPermissionAction = null
    }

    override fun listen(listener: (MatchEvent) -> Unit) { this.listener = listener }
    private fun emit(event: MatchEvent) { activity.runOnUiThread { listener(event) } }
    private fun withPermissions(discovery: Boolean, action: () -> Unit) {
        val required = buildList {
            if (Build.VERSION.SDK_INT >= 31) {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (discovery) {
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                if (Build.VERSION.SDK_INT >= 29) add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.NEARBY_WIFI_DEVICES)
            if (Build.VERSION.SDK_INT >= 37) add("android.permission.ACCESS_LOCAL_NETWORK")
        }.filter { activity.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
            .toMutableList()
        // Android 12+ requires both location permissions in the same request, even if coarse is already granted.
        if (discovery && Build.VERSION.SDK_INT >= 31 &&
            Manifest.permission.ACCESS_FINE_LOCATION in required &&
            Manifest.permission.ACCESS_COARSE_LOCATION !in required) {
            required.add(0, Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (required.isEmpty()) action() else {
            pendingPermissionAction = action
            permissionLauncher.launch(required.toTypedArray())
        }
    }

    private fun failure(action: String, error: Exception): MatchEvent.Error {
        val code = (error as? ApiException)?.statusCode
        val detail = when (code) {
            ConnectionsStatusCodes.MISSING_PERMISSION_ACCESS_COARSE_LOCATION -> "缺少粗略位置权限"
            ConnectionsStatusCodes.MISSING_PERMISSION_ACCESS_FINE_LOCATION -> "缺少精确位置权限"
            ConnectionsStatusCodes.MISSING_PERMISSION_BLUETOOTH_SCAN,
            ConnectionsStatusCodes.MISSING_PERMISSION_BLUETOOTH_CONNECT,
            ConnectionsStatusCodes.MISSING_PERMISSION_BLUETOOTH_ADVERTISE -> "缺少附近设备权限"
            ConnectionsStatusCodes.MISSING_PERMISSION_NEARBY_WIFI_DEVICES -> "缺少附近 Wi-Fi 设备权限"
            ConnectionsStatusCodes.STATUS_RADIO_ERROR -> "无线连接不可用，请检查蓝牙和 Wi-Fi"
            else -> code?.let { ConnectionsStatusCodes.getStatusCodeString(it) } ?: "未知错误"
        }
        return MatchEvent.Error("$action：$detail${code?.let { "（$it）" } ?: ""}")
    }

    private val connectionCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(id: String, info: ConnectionInfo) {
            if (endpointId != null && endpointId != id) {
                client.rejectConnection(id)
                return
            }
            endpointId = id
            incomingConnection = info.isIncomingConnection
            val remoteNonce = info.endpointName.substringAfterLast('|', "")
            activeNfc = pairing && !connectionCodeMode &&
                (id == nfcPeerId || remoteNonce == touchedReaderNonce)
            if (activeNfc) {
                emit(MatchEvent.Pairing("已接触，正在建立安全连接…"))
                accept(id)
            } else emit(MatchEvent.Verification(id, info.endpointName.substringBefore('|'),
                info.authenticationDigits))
        }
        override fun onConnectionResult(id: String, result: ConnectionResolution) {
            if (endpointId != id) return
            if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                client.stopAdvertising()
                client.stopDiscovery()
                disableReader()
                Log.i("TetrisMatch", "Nearby connection established")
                if (activeNfc) {
                    mainHandler.postDelayed({
                        if (endpointId == id && activeNfc && !pairingComplete) failNfcPairing()
                    }, 10_000)
                    if (!incomingConnection) {
                        val proof = nfcSecret ?: return failNfcPairing()
                        send("PAIR|${MessageDigest.getInstance("SHA-256").digest(proof).hexString()}")
                    }
                } else if (pairing) {
                    pairing = false
                    TetrisNfcBridge.end()
                    emit(MatchEvent.Paired(incomingConnection))
                }
                else emit(MatchEvent.Connected)
            } else {
                endpointId = null
                emit(MatchEvent.Error("连接失败，请重新寻找好友"))
            }
        }
        override fun onDisconnected(id: String) {
            if (endpointId != id) return
            endpointId = null
            activeNfc = false
            Log.w("TetrisMatch", "Nearby disconnected active peer")
            emit(MatchEvent.Disconnected)
        }
    }
    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(id: String, info: DiscoveredEndpointInfo) {
            val nonce = info.endpointName.substringAfterLast('|', "")
            if (pairing && nonce.length == 16 && nonce != ownNonce) {
                peerNonces[id] = nonce
                if (nfcAvailable && !connectionCodeMode && ownNonce > nonce) enableReader()
                if (nfcAvailable && !connectionCodeMode)
                    emit(MatchEvent.Pairing("已发现好友，请将手机背面轻触"))
            }
            emit(MatchEvent.Peer(id, info.endpointName.substringBefore('|')))
        }
        override fun onEndpointLost(id: String) { peerNonces.remove(id) }
    }
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(id: String, payload: Payload) {
            if (endpointId != id) return
            payload.asBytes()?.decodeToString()?.let { message ->
                when {
                    message.startsWith("PAIR|") && activeNfc && incomingConnection -> {
                        if (pairingComplete) {
                            send("PAIR_OK")
                            return@let
                        }
                        val expected = TetrisNfcBridge.secretFor(touchedReaderNonce ?: "")
                        val received = runCatching { message.substringAfter('|').hexBytes() }.getOrNull()
                        if (expected != null && received != null &&
                            MessageDigest.isEqual(MessageDigest.getInstance("SHA-256").digest(expected), received)) {
                            pairingComplete = true
                            pairing = false
                            TetrisNfcBridge.end()
                            send("PAIR_OK")
                            emit(MatchEvent.Paired(true))
                        } else failNfcPairing()
                    }
                    message == "PAIR_OK" && activeNfc && !incomingConnection -> {
                        pairingComplete = true
                        pairing = false
                        nfcSecret = null
                        TetrisNfcBridge.end()
                        emit(MatchEvent.Paired(false))
                    }
                    !message.startsWith("PAIR") -> emit(MatchEvent.Message(message))
                }
            }
        }
        override fun onPayloadTransferUpdate(id: String, update: PayloadTransferUpdate) {
            if (id == endpointId && update.status == PayloadTransferUpdate.Status.FAILURE) {
                Log.w("TetrisMatch", "Nearby payload transfer failed")
            }
        }
    }

    override fun host() = withPermissions(discovery = false) {
        pairing = false
        endpointId = null
        client.stopAllEndpoints()
        client.stopDiscovery()
        val name = "益智小岛 ${((1000..9999).random())}"
        client.startAdvertising(name, serviceId, connectionCallback,
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER)
                .setLowPower(true).setConnectionType(ConnectionType.NON_DISRUPTIVE).build())
            .addOnFailureListener { emit(failure("创建对战失败", it)) }
    }
    override fun discover() = withPermissions(discovery = true) {
        pairing = false
        endpointId = null
        client.stopAllEndpoints()
        client.stopAdvertising()
        client.startDiscovery(serviceId, discoveryCallback,
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER)
                .setLowPower(true).build())
            .addOnFailureListener { emit(failure("搜索好友失败", it)) }
    }
    override fun connect(peerId: String) {
        activeNfc = false
        nfcPeerId = null
        nfcSecret = null
        requestConnection(peerId)
    }
    override fun useConnectionCode() {
        connectionCodeMode = true
        disableReader()
        TetrisNfcBridge.end()
        nfcPeerId = null
        nfcSecret = null
        touchedReaderNonce = null
    }
    private fun requestConnection(peerId: String) {
        val name = if (pairing) "益智小岛|$ownNonce" else "益智小岛"
        client.requestConnection(name, peerId, connectionCallback,
            ConnectionOptions.Builder().setLowPower(true)
                .setConnectionType(ConnectionType.NON_DISRUPTIVE).build())
            .addOnFailureListener { emit(MatchEvent.Error("连接好友失败")) }
    }
    override fun accept(peerId: String) {
        client.acceptConnection(peerId, payloadCallback)
            .addOnFailureListener { emit(MatchEvent.Error("确认连接失败")) }
    }
    override fun reject(peerId: String) {
        if (endpointId == peerId) endpointId = null
        client.rejectConnection(peerId)
    }
    override fun send(message: String) {
        endpointId?.let { id -> client.sendPayload(id, Payload.fromBytes(message.encodeToByteArray()))
            .addOnFailureListener { error ->
                Log.w("TetrisMatch", "Payload rejected: ${(error as? ApiException)?.statusCode ?: "unknown"}")
            } }
    }
    override fun stop() {
        pairing = false
        connectionCodeMode = false
        disableReader()
        TetrisNfcBridge.end()
        peerNonces.clear()
        nfcPeerId = null
        nfcSecret = null
        touchedReaderNonce = null
        activeNfc = false
        pairingComplete = false
        pendingPermissionAction = null
        if (endpointId != null) Log.i("TetrisMatch", "App closed Nearby connection")
        endpointId = null
        client.stopAdvertising()
        client.stopDiscovery()
        client.stopAllEndpoints()
    }

    override fun pair() = withPermissions(discovery = true) {
        stop()
        pairing = true
        ownNonce = ByteArray(8).also { SecureRandom().nextBytes(it) }.hexString()
        TetrisNfcBridge.begin(ownNonce)
        TetrisNfcBridge.onTouch = { reader ->
            touchedReaderNonce = reader
            activity.runOnUiThread {
                if (pairing) {
                    emit(MatchEvent.Pairing("手机已接触，正在配对…"))
                }
            }
        }
        val name = "益智小岛|$ownNonce"
        client.startAdvertising(name, serviceId, connectionCallback,
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER)
                .setLowPower(true).setConnectionType(ConnectionType.NON_DISRUPTIVE).build())
            .addOnFailureListener { emit(failure("附近连接失败", it)) }
        client.startDiscovery(serviceId, discoveryCallback,
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER)
                .setLowPower(true).build())
            .addOnFailureListener { emit(failure("搜索好友失败", it)) }
    }

    private fun enableReader() {
        if (!readerEnabled.compareAndSet(false, true)) return
        val enabled = runCatching { nfcAdapter?.enableReaderMode(activity, { tag ->
            val response = runCatching {
                val iso = IsoDep.get(tag) ?: return@runCatching null
                iso.connect()
                try {
                    val select = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00,
                        TetrisNfcBridge.aid.size.toByte()) + TetrisNfcBridge.aid
                    val data = iso.transceive(select)
                    if (data.size != 26 || !data.takeLast(2).toByteArray()
                            .contentEquals(byteArrayOf(0x90.toByte(), 0x00))) return@runCatching null
                    val remote = data.copyOfRange(0, 8).hexString()
                    val proof = data.copyOfRange(8, 24)
                    val touch = byteArrayOf(0x80.toByte(), 0x10, 0x00, 0x00, 0x08) + ownNonce.hexBytes()
                    if (!iso.transceive(touch).contentEquals(byteArrayOf(0x90.toByte(), 0x00)))
                        return@runCatching null
                    remote to proof
                } finally { iso.close() }
            }.getOrNull()
            if (response != null) activity.runOnUiThread {
                if (pairing && endpointId == null) {
                    val peer = peerNonces.entries.firstOrNull { it.value == response.first }?.key
                    if (peer != null) {
                        nfcPeerId = peer
                        nfcSecret = response.second
                        activeNfc = true
                        emit(MatchEvent.Pairing("手机已接触，正在配对…"))
                        requestConnection(peer)
                    }
                }
            }
        }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null) }.isSuccess
        if (!enabled) {
            readerEnabled.set(false)
            emit(MatchEvent.Pairing("NFC 暂不可用，可改用连接码"))
        }
    }

    private fun disableReader() {
        if (readerEnabled.getAndSet(false)) runCatching { nfcAdapter?.disableReaderMode(activity) }
    }

    private fun failNfcPairing() {
        endpointId?.let(client::disconnectFromEndpoint)
        endpointId = null
        activeNfc = false
        pairing = false
        nfcSecret = null
        disableReader()
        TetrisNfcBridge.end()
        emit(MatchEvent.Error("NFC 配对验证失败，请重试或改用连接码"))
    }
}
