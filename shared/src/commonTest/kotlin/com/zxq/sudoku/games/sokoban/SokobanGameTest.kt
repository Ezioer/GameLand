package com.zxq.sudoku.games.sokoban

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SokobanGameTest {
    @Test
    fun allBuiltInLevelsCanBeSolved() {
        SokobanLevels.indices.forEach { index ->
            val initial = SokobanGame.start(index)
            val queue = ArrayDeque<SokobanGame>()
            val seen = mutableSetOf(initial.player to initial.crates)
            queue.add(initial)
            var solved = false
            while (queue.isNotEmpty() && !solved) {
                val current = queue.removeFirst()
                if (current.completed) { solved = true; break }
                SokobanDirection.entries.forEach { direction ->
                    val next = current.move(direction)
                    if (seen.add(next.player to next.crates)) {
                        queue.add(next.copy(history = emptyList()))
                    }
                }
            }
            assertTrue(solved, "第 ${index + 1} 关不可解")
        }
    }

    @Test
    fun pushCompletesLevelAndUndoRestoresState() {
        val start = SokobanGame.start(0)
        val completed = start.move(SokobanDirection.Up)
        assertTrue(completed.completed)
        assertEquals(1, completed.moves)
        assertEquals(1, completed.pushes)
        assertEquals(start.player, completed.undo().player)
        assertEquals(start.crates, completed.undo().crates)
        assertTrue(completed.undo().everCompleted)
        assertEquals(completed, completed.move(SokobanDirection.Up))
    }

    @Test
    fun wallsAndBlockedPushesDoNotCountAsMoves() {
        val start = SokobanGame.start(0)
        val atWall = start.move(SokobanDirection.Left).move(SokobanDirection.Left)
        assertEquals(atWall, atWall.move(SokobanDirection.Left))
        val crateAgainstWall = SokobanGame.start(0).copy(
            player = start.level.width + 2,
            crates = setOf(start.level.width + 1),
        )
        assertEquals(crateAgainstWall, crateAgainstWall.move(SokobanDirection.Left))
    }

    @Test
    fun saveRestoresProgressHistoryAndClock() {
        val start = SokobanGame.start(0)
        val progressed = start.move(SokobanDirection.Right).tick().tick()
        val restored = restoreSokobanGame(progressed.serialize())
        assertEquals(progressed, restored)
        assertEquals(start.copy(elapsedSeconds = 2), restored?.undo())
        assertEquals(null, restoreSokobanGame("v1|broken"))
    }
}
