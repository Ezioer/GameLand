package com.zxq.sudoku.games.sudoku

import kotlin.random.Random

enum class SudokuDifficulty(val label: String, val description: String, val clues: Int, val pattern: String) {
    Beginner("入门", "更多提示，适合熟悉规则", 48, "123050009406000120789023406034060001067801234890004067345008012600910000912045078"),
    Easy("简单", "轻松练习基础排除法", 42, "123456000056700100709003400034000800067801000001204067000678912008010305010345078"),
    Medium("中级", "需要交叉观察行、列与宫", 36, "120456000000780000709100050004007091500000234001230060305670910600010040012040008"),
    Hard("困难", "更少线索，需要耐心推理", 32, "003450789000789100080020400000507000060001030090204507005608000670010300010005000"),
    Expert("专家", "复杂局面，尝试候选数笔记", 28, "120000000406080003000003400004060891507000000000000560300608000078000000912340070"),
    Master("大师", "稀疏线索，挑战多步推理", 25, "023000009400000103000100050000500000567000000890004060000000910000002305000345008"),
}

data class SudokuPuzzle(val givens: List<Int>, val solution: List<Int>, val difficulty: SudokuDifficulty) {
    init {
        require(givens.size == 81 && solution.size == 81)
    }

    companion object {
        fun create(difficulty: SudokuDifficulty, random: Random = Random.Default): SudokuPuzzle {
            val bands = (0..2).shuffled(random)
            val stacks = (0..2).shuffled(random)
            val rows = bands.flatMap { band -> (0..2).shuffled(random).map { band * 3 + it } }
            val columns = stacks.flatMap { stack -> (0..2).shuffled(random).map { stack * 3 + it } }
            val digits = (1..9).shuffled(random)
            val transpose = random.nextBoolean()
            val baseSolution = List(81) { index ->
                val row = index / 9
                val column = index % 9
                (row * 3 + row / 3 + column) % 9 + 1
            }
            fun sourceIndex(index: Int): Int {
                val row = index / 9
                val column = index % 9
                return if (transpose) rows[column] * 9 + columns[row] else rows[row] * 9 + columns[column]
            }
            val givens = List(81) { index ->
                val value = difficulty.pattern[sourceIndex(index)].digitToInt()
                if (value == 0) 0 else digits[value - 1]
            }
            val solution = List(81) { index -> digits[baseSolution[sourceIndex(index)] - 1] }
            return SudokuPuzzle(givens, solution, difficulty)
        }
    }
}

data class SudokuMove(val cell: Int, val previousValue: Int, val previousNotes: Int)

internal enum class CompletedUnitType { Row, Column, Box }

internal data class CompletedUnit(val type: CompletedUnitType, val cells: List<Int>)

internal fun newlyCompletedUnits(before: List<Int>, after: List<Int>): List<CompletedUnit> {
    require(before.size == 81 && after.size == 81)
    if (before == after) return emptyList()
    val digits = (1..9).toSet()
    val units = buildList {
        repeat(9) { row -> add(CompletedUnit(CompletedUnitType.Row, (0..8).map { row * 9 + it })) }
        repeat(9) { column -> add(CompletedUnit(CompletedUnitType.Column, (0..8).map { it * 9 + column })) }
        repeat(9) { box ->
            val top = box / 3 * 3
            val left = box % 3 * 3
            add(CompletedUnit(CompletedUnitType.Box, (0..8).map { (top + it / 3) * 9 + left + it % 3 }))
        }
    }
    return units.filter { unit ->
        unit.cells.map { after[it] }.toSet() == digits && unit.cells.map { before[it] }.toSet() != digits
    }
}

internal fun newlyCompletedCells(before: List<Int>, after: List<Int>): Set<Int> =
    newlyCompletedUnits(before, after).flatMapTo(mutableSetOf()) { it.cells }

internal fun completionWaveAmount(unit: CompletedUnit, origin: Int, cell: Int, progress: Float): Float {
    if (origin !in unit.cells || cell !in unit.cells) return 0f
    val originRow = origin / 9
    val originColumn = origin % 9
    val rowDistance = kotlin.math.abs(cell / 9 - originRow)
    val columnDistance = kotlin.math.abs(cell % 9 - originColumn)
    val distance = when (unit.type) {
        CompletedUnitType.Row -> columnDistance.toFloat()
        CompletedUnitType.Column -> rowDistance.toFloat()
        CompletedUnitType.Box -> kotlin.math.sqrt((rowDistance * rowDistance + columnDistance * columnDistance).toFloat())
    }
    val farthest = when (unit.type) {
        CompletedUnitType.Row -> maxOf(originColumn, 8 - originColumn).toFloat()
        CompletedUnitType.Column -> maxOf(originRow, 8 - originRow).toFloat()
        CompletedUnitType.Box -> {
            val boxTop = originRow / 3 * 3
            val boxLeft = originColumn / 3 * 3
            val maxRow = maxOf(originRow - boxTop, boxTop + 2 - originRow)
            val maxColumn = maxOf(originColumn - boxLeft, boxLeft + 2 - originColumn)
            kotlin.math.sqrt((maxRow * maxRow + maxColumn * maxColumn).toFloat())
        }
    }
    val arrival = if (farthest == 0f) 0f else distance / farthest * 0.65f
    val local = ((progress - arrival) / 0.35f).coerceIn(0f, 1f)
    return local * local * (3f - 2f * local)
}

@ConsistentCopyVisibility
data class SudokuGame private constructor(
    val puzzle: SudokuPuzzle,
    val values: List<Int>,
    val notes: List<Int>,
    val selectedCell: Int? = null,
    val noteMode: Boolean = false,
    val mistakes: Int = 0,
    val elapsedSeconds: Int = 0,
    val paused: Boolean = false,
    private val moves: List<SudokuMove> = emptyList(),
) {
    val won: Boolean get() = values == puzzle.solution
    val finished: Boolean get() = won || mistakes >= 3
    val canUndo: Boolean get() = moves.isNotEmpty() && !finished

    fun select(cell: Int): SudokuGame = if (cell in 0..80 && !paused) copy(selectedCell = cell) else this

    fun toggleNotes(): SudokuGame = if (finished || paused) this else copy(noteMode = !noteMode)

    fun togglePause(): SudokuGame = if (finished) this else copy(paused = !paused)

    fun tick(): SudokuGame = if (finished || paused) this else copy(elapsedSeconds = elapsedSeconds + 1)

    fun remaining(digit: Int): Int = 9 - values.count { it == digit }

    fun revealOne(): SudokuGame {
        if (finished || paused) return this
        val cell = selectedCell?.takeIf { puzzle.givens[it] == 0 && values[it] == 0 }
            ?: values.indexOfFirst { it == 0 }
        if (cell < 0) return this
        val updatedValues = values.toMutableList()
        val updatedNotes = notes.toMutableList()
        updatedValues[cell] = puzzle.solution[cell]
        updatedNotes[cell] = 0
        return copy(
            values = updatedValues,
            notes = updatedNotes,
            selectedCell = cell,
            moves = moves + SudokuMove(cell, values[cell], notes[cell]),
        )
    }

    fun serialize(): String = listOf(
        puzzle.difficulty.name,
        puzzle.givens.joinToString(""),
        puzzle.solution.joinToString(""),
        values.joinToString(""),
        notes.joinToString(","),
        selectedCell ?: -1,
        noteMode,
        mistakes,
        elapsedSeconds,
        paused,
        moves.joinToString(";") { "${it.cell},${it.previousValue},${it.previousNotes}" },
    ).joinToString("|")

    fun enter(digit: Int): SudokuGame {
        val cell = selectedCell ?: return this
        if (digit !in 1..9 || puzzle.givens[cell] != 0 || finished || paused) return this
        if (noteMode) {
            if (values[cell] != 0) return this
            val updated = notes.toMutableList()
            updated[cell] = updated[cell] xor (1 shl digit)
            return copy(notes = updated, moves = moves + SudokuMove(cell, values[cell], notes[cell]))
        }
        if (values[cell] == digit) return this
        if (puzzle.solution[cell] != digit) return copy(mistakes = (mistakes + 1).coerceAtMost(3))
        val updatedValues = values.toMutableList()
        val updatedNotes = notes.toMutableList()
        updatedValues[cell] = digit
        updatedNotes[cell] = 0
        return copy(
            values = updatedValues,
            notes = updatedNotes,
            moves = moves + SudokuMove(cell, values[cell], notes[cell]),
        )
    }

    fun erase(): SudokuGame {
        val cell = selectedCell ?: return this
        if (puzzle.givens[cell] != 0 || finished || paused || (values[cell] == 0 && notes[cell] == 0)) return this
        val updatedValues = values.toMutableList()
        val updatedNotes = notes.toMutableList()
        updatedValues[cell] = 0
        updatedNotes[cell] = 0
        return copy(
            values = updatedValues,
            notes = updatedNotes,
            moves = moves + SudokuMove(cell, values[cell], notes[cell]),
        )
    }

    fun undo(): SudokuGame {
        if (!canUndo || paused) return this
        val last = moves.last()
        val updatedValues = values.toMutableList()
        val updatedNotes = notes.toMutableList()
        updatedValues[last.cell] = last.previousValue
        updatedNotes[last.cell] = last.previousNotes
        return copy(values = updatedValues, notes = updatedNotes, moves = moves.dropLast(1), selectedCell = last.cell)
    }

    companion object {
        fun start(puzzle: SudokuPuzzle): SudokuGame =
            SudokuGame(puzzle = puzzle, values = puzzle.givens.toList(), notes = List(81) { 0 })

        fun restore(saved: String): SudokuGame? = runCatching {
            if (saved.isEmpty()) return null
            val parts = saved.split('|')
            require(parts.size == 11)
            val difficulty = SudokuDifficulty.entries.first { it.name == parts[0] }
            fun digits(value: String): List<Int> = value.map { it.digitToInt() }.also { require(it.size == 81) }
            val puzzle = SudokuPuzzle(digits(parts[1]), digits(parts[2]), difficulty)
            val notes = parts[4].split(',').map { it.toInt() }.also { require(it.size == 81) }
            val selected = parts[5].toInt().takeIf { it >= 0 }
            val moves = if (parts[10].isEmpty()) emptyList() else parts[10].split(';').map { entry ->
                val fields = entry.split(',').map { it.toInt() }
                require(fields.size == 3)
                SudokuMove(fields[0], fields[1], fields[2])
            }
            SudokuGame(
                puzzle = puzzle,
                values = digits(parts[3]),
                notes = notes,
                selectedCell = selected,
                noteMode = parts[6].toBooleanStrict(),
                mistakes = parts[7].toInt(),
                elapsedSeconds = parts[8].toInt(),
                paused = parts[9].toBooleanStrict(),
                moves = moves,
            )
        }.getOrNull()
    }
}
