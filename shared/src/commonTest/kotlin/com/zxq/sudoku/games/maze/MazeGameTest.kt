package com.zxq.sudoku.games.maze

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MazeGameTest {
    @Test
    fun everyDifficultyHasAReachableExit() {
        MazeDifficulty.entries.forEach { difficulty ->
            val start = MazeGame.start(difficulty) { 0 }
            assertFalse(start.start in start.walls)
            assertFalse(start.exit in start.walls)
            assertTrue((0 until start.size).all { x -> x in start.walls })
            assertTrue((0 until start.size).all { y -> y * start.size in start.walls })
            val path = start.shortestPath()
            assertTrue(path.isNotEmpty(), "${difficulty.label}迷宫没有出口路径")

            val solved = path.fold(start) { game, cell ->
                val direction = MazeDirection.entries.first { game.neighbor(game.player, it) == cell }
                game.move(direction)
            }
            assertTrue(solved.completed)
            assertEquals(path.size, solved.moves)
            assertEquals(path.size, solved.history.size)
        }
    }

    @Test
    fun wallsUndoHintAndRestartFollowTheRules() {
        val start = MazeGame.start(MazeDifficulty.Easy) { 0 }
        assertEquals(start, start.move(MazeDirection.Up))
        assertEquals(start, start.move(MazeDirection.Left))
        val first = start.shortestPath().first()
        val hinted = start.hint()
        assertEquals(first, hinted.hintCell)
        assertEquals(1, hinted.hintsUsed)
        assertEquals(hinted, hinted.hint())

        val direction = MazeDirection.entries.first { start.neighbor(start.player, it) == first }
        val moved = hinted.move(direction).tick()
        assertEquals(first, moved.player)
        assertEquals(1, moved.moves)
        assertEquals(1, moved.elapsedSeconds)
        assertEquals(null, moved.hintCell)
        assertEquals(start.start, moved.undo().player)
        assertEquals(0, moved.undo().moves)
        assertEquals(start.walls, moved.restart().walls)
        assertEquals(start.start, moved.restart().player)
        assertEquals(0, moved.restart().elapsedSeconds)
    }

    @Test
    fun progressCanBeRestoredAndInvalidProgressIsRejected() {
        val start = MazeGame.start(MazeDifficulty.Medium) { 0 }
        val next = start.shortestPath().first()
        val direction = MazeDirection.entries.first { start.neighbor(start.player, it) == next }
        val progressed = start.hint().move(direction).tick()
        assertEquals(progressed, assertNotNull(restoreMazeGame(progressed.serialize())))
        assertEquals(null, restoreMazeGame("v1|broken"))
    }
}
