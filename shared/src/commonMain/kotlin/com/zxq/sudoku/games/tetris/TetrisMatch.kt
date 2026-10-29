package com.zxq.sudoku.games.tetris

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import kotlin.random.Random
import kotlin.time.TimeSource

/** Platform adapters only carry bytes. Rules, messages and UI state stay in commonMain. */
interface MatchTransport {
    val available: Boolean
    val nfcAvailable: Boolean get() = false
    fun listen(listener: (MatchEvent) -> Unit)
    fun pair() = discover()
    fun useConnectionCode() = Unit
    fun host()
    fun discover()
    fun connect(peerId: String)
    fun accept(peerId: String)
    fun reject(peerId: String)
    fun send(message: String)
    fun stop()
}

sealed interface MatchEvent {
    data class Peer(val id: String, val name: String) : MatchEvent
    data class Verification(val id: String, val name: String, val digits: String) : MatchEvent
    data class Pairing(val message: String) : MatchEvent
    data object Connected : MatchEvent
    data class Paired(val isHost: Boolean) : MatchEvent
    data object Disconnected : MatchEvent
    data class Message(val value: String) : MatchEvent
    data class Error(val message: String) : MatchEvent
}

object UnavailableMatchTransport : MatchTransport {
    override val available = false
    override fun listen(listener: (MatchEvent) -> Unit) = Unit
    override fun host() = Unit
    override fun discover() = Unit
    override fun connect(peerId: String) = Unit
    override fun accept(peerId: String) = Unit
    override fun reject(peerId: String) = Unit
    override fun send(message: String) = Unit
    override fun stop() = Unit
}

internal enum class MatchPhase { Idle, Searching, Connecting, Verify, Connected, Playing, Finished, Disconnected }
internal enum class MatchResult { Win, Lose, Draw }

internal class TetrisMatch(private val transport: MatchTransport) {
    val available get() = transport.available
    val nfcAvailable get() = transport.nfcAvailable
    var phase by mutableStateOf(MatchPhase.Idle)
        private set
    var isHost by mutableStateOf(false)
        private set
    var peers by mutableStateOf<List<MatchEvent.Peer>>(emptyList())
        private set
    var verification by mutableStateOf<MatchEvent.Verification?>(null)
        private set
    var remoteGame by mutableStateOf<TetrisGame?>(null)
        private set
    var result by mutableStateOf<MatchResult?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var remoteStale by mutableStateOf(false)
        private set
    var pendingGarbage by mutableStateOf(0)
        private set
    var pairingMessage by mutableStateOf<String?>(null)
        private set
    var showFallback by mutableStateOf(false)
        private set
    private var automaticStart = false
    private var matchId = ""
    private var outgoingSequence = 0
    private var incomingSequence = -1
    private var localGame: TetrisGame? = null
    private var pendingState = false
    private var lastRemoteState = TimeSource.Monotonic.markNow()
    private var lastStateSent = TimeSource.Monotonic.markNow()
    private var lastControlSent = TimeSource.Monotonic.markNow()
    private var startAcknowledged = false
    private var resultAcknowledged = false

    fun attach() = transport.listen(::onEvent)
    fun pair() {
        reset()
        automaticStart = true
        phase = MatchPhase.Searching
        pairingMessage = "正在发现附近好友…"
        showFallback = !nfcAvailable
        transport.pair()
    }
    fun useConnectionCode() {
        showFallback = true
        pairingMessage = "选择一台手机上的好友，双方核对连接码"
        transport.useConnectionCode()
    }
    fun host() {
        reset()
        isHost = true
        phase = MatchPhase.Searching
        transport.host()
    }
    fun discover() {
        reset()
        isHost = false
        phase = MatchPhase.Searching
        transport.discover()
    }
    fun connect(id: String) {
        if (phase != MatchPhase.Searching) return
        phase = MatchPhase.Connecting
        transport.connect(id)
    }
    fun accept() {
        val request = verification ?: return
        verification = null
        phase = MatchPhase.Connecting
        transport.accept(request.id)
    }
    fun reject() {
        verification?.let { transport.reject(it.id) }
        verification = null
        phase = MatchPhase.Searching
    }
    fun start() {
        if (!isHost || phase != MatchPhase.Connected) return
        matchId = Random.nextLong().toString(16)
        phase = MatchPhase.Playing
        lastRemoteState = TimeSource.Monotonic.markNow()
        sendStart()
    }
    fun publish(game: TetrisGame) {
        if (phase != MatchPhase.Playing || matchId.isEmpty()) return
        val cleared = game.lines > (localGame?.lines ?: 0)
        pendingState = pendingState || localGame != game
        localGame = game
        if (outgoingSequence == 0 || cleared || game.finished) flushPendingState()
        updateResult(game, remoteGame)
    }
    fun takeIncomingGarbage(): Int = pendingGarbage.also { pendingGarbage = 0 }
    /** Called by the visible match screen; collapses rapid moves into the latest board. */
    fun maintenanceTick(remoteAgeMs: Long = lastRemoteState.elapsedNow().inWholeMilliseconds) {
        if (phase != MatchPhase.Playing && phase != MatchPhase.Finished) return
        // A delayed snapshot is not proof of a dropped Nearby connection.
        remoteStale = phase == MatchPhase.Playing && remoteAgeMs > 5_000
        if (phase == MatchPhase.Playing && lastStateSent.elapsedNow().inWholeMilliseconds >= 300 &&
            (pendingState || lastStateSent.elapsedNow().inWholeMilliseconds >= 2_000)) {
            flushPendingState()
        }
        if (lastControlSent.elapsedNow().inWholeMilliseconds >= 1_000) {
            retryPendingControl()
        }
    }
    internal fun retryPendingControl() {
        if (phase != MatchPhase.Playing && phase != MatchPhase.Finished) return
        when {
            isHost && !startAcknowledged && phase == MatchPhase.Playing -> sendStart()
            isHost && result != null && !resultAcknowledged -> sendResult()
            else -> {
                transport.send("v1|PING|$matchId")
                lastControlSent = TimeSource.Monotonic.markNow()
            }
        }
    }
    internal fun flushPendingState() {
        val game = localGame ?: return
        transport.send("v1|STATE|$matchId|${outgoingSequence++}|${game.serialize()}")
        pendingState = false
        lastStateSent = TimeSource.Monotonic.markNow()
    }
    private fun sendStart() {
        transport.send("v1|START|$matchId")
        lastControlSent = TimeSource.Monotonic.markNow()
    }
    private fun sendResult() {
        val outcome = result ?: return
        transport.send("v1|RESULT|$matchId|${outcome.name}")
        lastControlSent = TimeSource.Monotonic.markNow()
    }
    fun leave() {
        transport.stop()
        reset()
    }
    private fun reset() {
        phase = MatchPhase.Idle
        peers = emptyList()
        verification = null
        remoteGame = null
        result = null
        error = null
        matchId = ""
        outgoingSequence = 0
        incomingSequence = -1
        localGame = null
        pendingState = false
        pendingGarbage = 0
        remoteStale = false
        startAcknowledged = false
        resultAcknowledged = false
        pairingMessage = null
        showFallback = false
        automaticStart = false
    }
    private fun onEvent(event: MatchEvent) {
        when (event) {
            is MatchEvent.Pairing -> pairingMessage = event.message
            is MatchEvent.Peer -> if (phase == MatchPhase.Searching && peers.none { it.id == event.id })
                peers = peers + event
            is MatchEvent.Verification -> if (phase == MatchPhase.Searching || phase == MatchPhase.Connecting) {
                verification = event; phase = MatchPhase.Verify
            }
            MatchEvent.Connected -> if (phase == MatchPhase.Verify || phase == MatchPhase.Connecting) {
                verification = null; phase = MatchPhase.Connected
            }
            is MatchEvent.Paired -> if (phase == MatchPhase.Searching || phase == MatchPhase.Verify ||
                phase == MatchPhase.Connecting) {
                verification = null
                isHost = event.isHost
                phase = MatchPhase.Connected
                if (automaticStart && isHost) start()
            }
            MatchEvent.Disconnected -> if (phase == MatchPhase.Connecting || phase == MatchPhase.Verify ||
                phase == MatchPhase.Connected ||
                phase == MatchPhase.Playing || phase == MatchPhase.Finished) {
                phase = MatchPhase.Disconnected
                error = "连接已断开，本局对战已结束"
            }
            is MatchEvent.Error -> {
                error = event.message
                phase = if (phase == MatchPhase.Playing) MatchPhase.Disconnected else MatchPhase.Idle
            }
            is MatchEvent.Message -> receive(event.value)
        }
    }
    private fun receive(value: String) {
        val parts = value.split('|', limit = 5)
        if (parts.firstOrNull() != "v1") return
        when (parts.getOrNull(1)) {
            "START" -> if (!isHost && phase == MatchPhase.Connected && parts.size == 3) {
                matchId = parts[2]
                phase = MatchPhase.Playing
                lastRemoteState = TimeSource.Monotonic.markNow()
                transport.send("v1|START_ACK|$matchId")
            } else if (!isHost && (phase == MatchPhase.Playing || phase == MatchPhase.Finished) &&
                parts.size == 3 && parts[2] == matchId) {
                transport.send("v1|START_ACK|$matchId")
            }
            "START_ACK" -> if (isHost && parts.size == 3 && parts[2] == matchId) {
                startAcknowledged = true
            }
            "STATE" -> {
                if (phase != MatchPhase.Playing || parts.size != 5 || parts[2] != matchId) return
                val sequence = parts[3].toIntOrNull() ?: return
                if (sequence <= incomingSequence) return
                val game = restoreTetrisGame(parts[4]) ?: return
                incomingSequence = sequence
                val cleared = (game.lines.toLong() - (remoteGame?.lines ?: 0).toLong())
                    .coerceIn(0, TetrisHeight.toLong()).toInt()
                pendingGarbage = (pendingGarbage + cleared).coerceAtMost(TetrisHeight)
                remoteGame = game
                remoteStale = false
                lastRemoteState = TimeSource.Monotonic.markNow()
                localGame?.let { updateResult(it, game) }
            }
            "PING" -> if (parts.size == 3 && parts[2] == matchId &&
                (phase == MatchPhase.Playing || phase == MatchPhase.Finished)) {
            }
            "RESULT" -> if (!isHost && (phase == MatchPhase.Playing || phase == MatchPhase.Finished) &&
                parts.size == 4 && parts[2] == matchId) {
                val hostResult = MatchResult.entries.firstOrNull { it.name == parts[3] } ?: return
                transport.send("v1|RESULT_ACK|$matchId")
                result = when (hostResult) {
                    MatchResult.Win -> MatchResult.Lose
                    MatchResult.Lose -> MatchResult.Win
                    MatchResult.Draw -> MatchResult.Draw
                }
                phase = MatchPhase.Finished
            }
            "RESULT_ACK" -> if (isHost && parts.size == 3 && parts[2] == matchId) {
                resultAcknowledged = true
            }
        }
    }
    fun updateResult(local: TetrisGame, remote: TetrisGame?) {
        if (!isHost || phase != MatchPhase.Playing || remote == null) return
        result = when {
            local.finished && remote.finished -> MatchResult.Draw
            local.finished -> MatchResult.Lose
            remote.finished -> MatchResult.Win
            else -> null
        }
        if (result != null) {
            phase = MatchPhase.Finished
            sendResult()
        }
    }
}

@Composable
internal fun rememberTetrisMatch(transport: MatchTransport): TetrisMatch {
    val match = remember(transport) { TetrisMatch(transport) }
    DisposableEffect(match) {
        match.attach()
        onDispose { match.leave() }
    }
    return match
}
