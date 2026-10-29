package com.zxq.sudoku

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import java.security.SecureRandom

/** NFC only exchanges a short-lived pairing secret; game traffic stays on Nearby. */
internal object TetrisNfcBridge {
    val aid = byteArrayOf(0xF0.toByte(), 0x53, 0x55, 0x44, 0x4F, 0x4B, 0x55, 0x31)
    private val success = byteArrayOf(0x90.toByte(), 0x00)
    private val failure = byteArrayOf(0x6A, 0x82.toByte())
    @Volatile private var nonce: ByteArray? = null
    @Volatile private var secret: ByteArray? = null
    @Volatile private var touchedReader: String? = null
    @Volatile var onTouch: ((String) -> Unit)? = null

    fun begin(localNonce: String) {
        touchedReader = null
        nonce = localNonce.hexBytes()
        secret = ByteArray(16).also { SecureRandom().nextBytes(it) }
    }

    fun end() {
        nonce = null
        secret = null
        touchedReader = null
        onTouch = null
    }

    fun secretFor(readerNonce: String): ByteArray? =
        if (readerNonce == touchedReader) secret?.copyOf() else null

    fun process(command: ByteArray): ByteArray {
        val own = nonce ?: return failure
        val key = secret ?: return failure
        if (command.size == aid.size + 5 && command[0] == 0x00.toByte() &&
            command[1] == 0xA4.toByte() && command[2] == 0x04.toByte() &&
            command[4].toInt() == aid.size && command.copyOfRange(5, command.size).contentEquals(aid)) {
            return own + key + success
        }
        if (command.size == 13 && command[0] == 0x80.toByte() &&
            command[1] == 0x10.toByte() && command[4] == 0x08.toByte()) {
            touchedReader = command.copyOfRange(5, 13).hexString()
            onTouch?.invoke(touchedReader ?: return failure)
            return success
        }
        return failure
    }
}

internal fun ByteArray.hexString(): String = joinToString("") { "%02x".format(it.toInt() and 0xff) }
internal fun String.hexBytes(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

class TetrisNfcService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray =
        TetrisNfcBridge.process(commandApdu)

    override fun onDeactivated(reason: Int) = Unit
}
