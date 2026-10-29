package com.zxq.sudoku.games.maze

import kotlin.test.Test
import kotlin.test.assertEquals

class MazeStatisticsTest {
    @Test
    fun completionIsCountedOnceAndBestResultIsSaved() {
        val start = MazeGame.start(MazeDifficulty.Easy) { 0 }
        val initial = MazeStatistics().started(MazeDifficulty.Easy)
        val solved = start.shortestPath().fold(start) { game, cell ->
            val direction = MazeDirection.entries.first { game.neighbor(game.player, it) == cell }
            game.move(direction)
        }
        val completed = initial.progressed(start, solved, "2026-10-26 12:00")
        val record = completed.forDifficulty(MazeDifficulty.Easy)
        assertEquals(1, record.played)
        assertEquals(1, record.completed)
        assertEquals(solved.moves, record.bestMoves)
        assertEquals("2026-10-26 12:00", record.bestAt)
        assertEquals(completed, completed.progressed(solved, solved, "later"))
        assertEquals(completed, MazeStatistics.restore(completed.serialize()))
    }
}
