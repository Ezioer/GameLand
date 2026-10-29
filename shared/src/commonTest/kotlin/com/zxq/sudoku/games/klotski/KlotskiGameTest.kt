package com.zxq.sudoku.games.klotski

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KlotskiGameTest {
    @Test
    fun allLevelsUseLegalClassicPiecesAndCanReachExit() {
        KlotskiLevels.indices.forEach { index ->
            val start = KlotskiGame.start(index)
            assertTrue(validKlotskiPositions(start.positions))
            val queue = ArrayDeque<KlotskiGame>()
            val seen = mutableSetOf(canonical(start.positions))
            queue.add(start)
            var solved = false
            while (queue.isNotEmpty() && !solved) {
                val current = queue.removeFirst()
                if (current.completed) { solved = true; break }
                KlotskiPieces.indices.forEach { piece ->
                    KlotskiDirection.entries.forEach { direction ->
                        val next = current.move(piece, direction)
                        if (seen.add(canonical(next.positions))) {
                            queue.add(next.copy(history = emptyList()))
                        }
                    }
                }
            }
            assertTrue(solved, "第 ${index + 1} 关不可解")
        }
    }

    @Test
    fun pieceCannotCrossOtherPiecesOrBoardEdge() {
        val game = KlotskiGame.start(KlotskiLevels.lastIndex)
        assertEquals(game, game.move(0, KlotskiDirection.Down))
        assertEquals(game, game.move(1, KlotskiDirection.Left))
        val moved = game.move(8, KlotskiDirection.Right)
        assertEquals(1, moved.moves)
        assertTrue(validKlotskiPositions(moved.positions))
    }

    @Test
    fun caoCaoExitsAndUndoKeepsSingleCompletionMarker() {
        val start = KlotskiGame.start(0)
        assertFalse(start.completed)
        val won = start.move(0, KlotskiDirection.Right)
        assertTrue(won.completed)
        assertTrue(won.everCompleted)
        assertEquals(1, won.moves)
        assertEquals(start.positions, won.undo().positions)
        assertTrue(won.undo().everCompleted)
        assertEquals(won, won.move(0, KlotskiDirection.Left))
    }

    @Test
    fun multiCellSlideCountsAsOneMoveAndSaveRestoresHistory() {
        val start = KlotskiGame.start(KlotskiLevels.lastIndex)
        val moved = start.move(8, KlotskiDirection.Right, steps = 2).tick()
        assertEquals(1, moved.moves)
        assertEquals(1, moved.elapsedSeconds)
        val restored = assertNotNull(restoreKlotskiGame(moved.serialize()))
        assertEquals(moved, restored)
        assertEquals(start.positions, restored.undo().positions)
        assertEquals(null, restoreKlotskiGame("v1|broken"))
    }

    private fun canonical(positions: List<Int>) = listOf(
        positions[0].toString(),
        positions.subList(1, 5).sorted().joinToString(","),
        positions[5].toString(),
        positions.subList(6, 10).sorted().joinToString(","),
    ).joinToString("|")
}
