package com.zxq.sudoku.games.tetris

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TetrisMatchTest {
    private class FakeTransport : MatchTransport {
        override val available = true
        var listener: (MatchEvent) -> Unit = {}
        var other: FakeTransport? = null
        var pairCalls = 0
        var connectionCodeCalls = 0
        var dropNextStart = false
        var dropNextResult = false
        var stopCalls = 0
        val sent = mutableListOf<String>()
        override fun listen(listener: (MatchEvent) -> Unit) { this.listener = listener }
        override fun pair() { pairCalls++ }
        override fun useConnectionCode() { connectionCodeCalls++ }
        override fun host() = Unit
        override fun discover() = Unit
        override fun connect(peerId: String) = Unit
        override fun accept(peerId: String) = Unit
        override fun reject(peerId: String) = Unit
        override fun send(message: String) {
            sent += message
            if (dropNextStart && message.startsWith("v1|START|")) {
                dropNextStart = false
                return
            }
            if (dropNextResult && message.startsWith("v1|RESULT|")) {
                dropNextResult = false
                return
            }
            other?.listener?.invoke(MatchEvent.Message(message))
        }
        override fun stop() { stopCalls++ }
    }

    @Test
    fun nearbyPairingStartsGameAutomaticallyAfterBothSidesAreVerified() {
        val a = FakeTransport()
        val b = FakeTransport()
        a.other = b
        b.other = a
        val host = TetrisMatch(a)
        val guest = TetrisMatch(b)
        host.attach()
        guest.attach()
        host.pair()
        guest.pair()

        assertEquals(1, a.pairCalls)
        assertEquals(1, b.pairCalls)
        assertEquals(MatchPhase.Searching, host.phase)
        assertEquals(MatchPhase.Searching, guest.phase)
        a.listener(MatchEvent.Pairing("手机已接触"))
        assertEquals("手机已接触", host.pairingMessage)
        b.listener(MatchEvent.Paired(false))
        a.listener(MatchEvent.Paired(true))

        assertTrue(host.isHost)
        assertFalse(guest.isHost)
        assertEquals(MatchPhase.Playing, host.phase)
        assertEquals(MatchPhase.Playing, guest.phase)
    }

    @Test
    fun connectionCodeFallbackStillRequiresConfirmation() {
        val transport = FakeTransport()
        val match = TetrisMatch(transport)
        match.attach()
        match.pair()
        match.useConnectionCode()
        transport.listener(MatchEvent.Peer("friend", "好友"))
        assertTrue(match.showFallback)
        assertEquals(1, transport.connectionCodeCalls)
        match.connect("friend")
        transport.listener(MatchEvent.Verification("friend", "好友", "1234"))
        assertEquals(MatchPhase.Verify, match.phase)
        match.accept()
        transport.listener(MatchEvent.Paired(false))
        assertEquals(MatchPhase.Connected, match.phase)
    }

    @Test
    fun connectingShowsWaitingPhaseUntilBothSidesConfirm() {
        val transport = FakeTransport()
        val match = TetrisMatch(transport)
        match.attach()
        match.discover()

        match.connect("friend")
        assertEquals(MatchPhase.Connecting, match.phase)
        transport.listener(MatchEvent.Verification("friend", "好友", "1234"))
        assertEquals(MatchPhase.Verify, match.phase)
        match.accept()
        assertEquals(MatchPhase.Connecting, match.phase)
        transport.listener(MatchEvent.Connected)
        assertEquals(MatchPhase.Connected, match.phase)
    }

    @Test
    fun hostStartsGuestAndSharesBoardWithoutSharingControls() {
        val a = FakeTransport()
        val b = FakeTransport()
        a.other = b
        b.other = a
        val host = TetrisMatch(a)
        val guest = TetrisMatch(b)
        host.attach()
        guest.attach()
        host.host()
        guest.discover()
        a.listener(MatchEvent.Verification("b", "好友", "1234"))
        b.listener(MatchEvent.Verification("a", "好友", "1234"))
        a.listener(MatchEvent.Connected)
        b.listener(MatchEvent.Connected)
        host.start()
        assertEquals(MatchPhase.Playing, guest.phase)

        val game = TetrisGame.start { 0 }
        host.publish(game)
        assertEquals(game, guest.remoteGame)
        assertNull(guest.result)
        val older = a.sent.last()
        val moved = game.copy(active = game.active.copy(x = game.active.x + 1))
        host.publish(moved)
        assertEquals(game, guest.remoteGame)
        host.flushPendingState()
        b.listener(MatchEvent.Message(older))
        assertEquals(moved, guest.remoteGame)
    }

    @Test
    fun clearedLinesSendGarbageWithoutEndingMatchOrDoubleCountingSnapshots() {
        val a = FakeTransport()
        val b = FakeTransport()
        a.other = b
        b.other = a
        val host = TetrisMatch(a)
        val guest = TetrisMatch(b)
        host.attach()
        guest.attach()
        host.host()
        guest.discover()
        a.listener(MatchEvent.Verification("b", "好友", "1234"))
        b.listener(MatchEvent.Verification("a", "好友", "1234"))
        a.listener(MatchEvent.Connected)
        b.listener(MatchEvent.Connected)
        host.start()

        val starting = TetrisGame.start { 0 }
        host.publish(starting)
        guest.publish(starting)
        host.publish(starting.copy(lines = 10))
        assertNull(host.result)
        assertNull(guest.result)
        assertEquals(MatchPhase.Playing, host.phase)
        assertEquals(10, guest.remoteGame?.lines)
        assertEquals(10, guest.takeIncomingGarbage())

        b.listener(MatchEvent.Message(a.sent.last()))
        assertEquals(0, guest.takeIncomingGarbage())
        host.publish(starting.copy(lines = 11))
        assertEquals(1, guest.takeIncomingGarbage())

        val guestWithGarbage = starting.addGarbage(1) { 4 }
        guest.publish(guestWithGarbage)
        guest.flushPendingState()
        assertEquals(Tetromino.G, host.remoteGame?.board?.last())

        guest.publish(guestWithGarbage.copy(finished = true))
        assertEquals(MatchResult.Win, host.result)
        assertEquals(MatchResult.Lose, guest.result)
    }

    @Test
    fun lostResultIsResentUntilGuestAcknowledgesIt() {
        val a = FakeTransport()
        val b = FakeTransport()
        a.other = b
        b.other = a
        val host = TetrisMatch(a)
        val guest = TetrisMatch(b)
        host.attach()
        guest.attach()
        host.host()
        guest.discover()
        a.listener(MatchEvent.Verification("b", "好友", "1234"))
        b.listener(MatchEvent.Verification("a", "好友", "1234"))
        a.listener(MatchEvent.Connected)
        b.listener(MatchEvent.Connected)
        host.start()
        val starting = TetrisGame.start { 0 }
        host.publish(starting)
        guest.publish(starting)

        a.dropNextResult = true
        host.publish(starting.copy(finished = true))
        assertNull(guest.result)

        host.retryPendingControl()
        assertEquals(MatchResult.Win, guest.result)
        assertEquals(MatchPhase.Finished, guest.phase)
    }

    @Test
    fun lostStartIsResentUntilGuestAcknowledgesIt() {
        val a = FakeTransport()
        val b = FakeTransport()
        a.other = b
        b.other = a
        val host = TetrisMatch(a)
        val guest = TetrisMatch(b)
        host.attach()
        guest.attach()
        host.host()
        guest.discover()
        a.listener(MatchEvent.Verification("b", "好友", "1234"))
        b.listener(MatchEvent.Verification("a", "好友", "1234"))
        a.listener(MatchEvent.Connected)
        b.listener(MatchEvent.Connected)

        a.dropNextStart = true
        host.start()
        assertEquals(MatchPhase.Connected, guest.phase)

        host.retryPendingControl()
        assertEquals(MatchPhase.Playing, guest.phase)
    }

    @Test
    fun staleBoardDoesNotEndMatchButRealDisconnectDoes() {
        val transport = FakeTransport()
        val match = TetrisMatch(transport)
        match.attach()
        match.discover()
        transport.listener(MatchEvent.Verification("friend", "好友", "1234"))
        match.accept()
        transport.listener(MatchEvent.Connected)
        transport.listener(MatchEvent.Message("v1|START|test-match"))

        match.maintenanceTick(remoteAgeMs = 9_000)
        assertTrue(match.remoteStale)
        assertEquals(MatchPhase.Playing, match.phase)
        assertEquals(0, transport.stopCalls)

        val game = TetrisGame.start { 0 }
        transport.listener(MatchEvent.Message("v1|STATE|test-match|0|${game.serialize()}"))
        assertFalse(match.remoteStale)
        assertEquals(game, match.remoteGame)

        transport.listener(MatchEvent.Disconnected)
        assertEquals(MatchPhase.Disconnected, match.phase)
    }
}
