package com.zxq.sudoku.games.sudoku

internal enum class HintTechnique(val label: String) {
    NakedSingle("唯一候选数"),
    HiddenSingle("唯一位置"),
    LockedCandidate("锁定候选数"),
    NakedPair("显性数对"),
    HiddenPair("隐性数对"),
    XWing("X-Wing"),
    XYWing("XY-Wing"),
}

internal data class HintStep(val text: String, val cells: Set<Int>)
internal data class SudokuHint(
    val technique: HintTechnique,
    val targetCell: Int,
    val digit: Int,
    val isPlacement: Boolean,
    val steps: List<HintStep>,
)

internal fun findSudokuHint(game: SudokuGame): SudokuHint? =
    if (game.finished || game.paused) null else findSudokuHint(game.values, game.puzzle.difficulty)

internal fun findSudokuHint(values: List<Int>, difficulty: SudokuDifficulty): SudokuHint? {
    require(values.size == 81)
    val solver = HintSolver(values)
    solver.nakedSingle()?.let { return it }
    solver.hiddenSingle()?.let { return it }
    if (difficulty == SudokuDifficulty.Beginner || difficulty == SudokuDifficulty.Easy) return null
    solver.lockedCandidate()?.let { return it }
    solver.nakedPair()?.let { return it }
    solver.hiddenPair()?.let { return it }
    if (difficulty == SudokuDifficulty.Medium) return null
    solver.xWing()?.let { return it }
    return solver.xyWing()
}

internal class HintSolver(private val values: List<Int>, private val candidateOverride: List<Set<Int>>? = null) {
    init {
        require(values.size == 81 && (candidateOverride == null || candidateOverride.size == 81))
    }
    private val units: List<List<Int>> = buildList {
        repeat(9) { row -> add((0..8).map { row * 9 + it }) }
        repeat(9) { column -> add((0..8).map { it * 9 + column }) }
        repeat(9) { box ->
            val firstRow = box / 3 * 3
            val firstColumn = box % 3 * 3
            add((0..8).map { (firstRow + it / 3) * 9 + firstColumn + it % 3 })
        }
    }
    private val candidates = candidateOverride ?: List(81) { cell ->
        if (values[cell] != 0) emptySet() else (1..9).filterTo(mutableSetOf()) { digit ->
            units.filter { cell in it }.all { unit -> unit.none { values[it] == digit } }
        }
    }

    private fun location(cell: Int) = "第${cell / 9 + 1}行第${cell % 9 + 1}列"
    private fun unitName(index: Int): String = when {
        index < 9 -> "第${index + 1}行"
        index < 18 -> "第${index - 8}列"
        else -> "第${index - 17}宫"
    }
    private fun sees(first: Int, second: Int): Boolean = first / 9 == second / 9 ||
        first % 9 == second % 9 ||
        (first / 27 == second / 27 && first % 9 / 3 == second % 9 / 3)

    fun nakedSingle(): SudokuHint? {
        val cell = (0..80).firstOrNull { candidates[it].size == 1 } ?: return null
        val digit = candidates[cell].first()
        return SudokuHint(HintTechnique.NakedSingle, cell, digit, true, listOf(
            HintStep("观察${location(cell)}所在的行、列和宫。", setOf(cell)),
            HintStep("排除这些区域已经出现的数字后，这一格只剩一个候选数。", setOf(cell)),
            HintStep("${location(cell)}可以填 $digit。", setOf(cell)),
        ))
    }

    fun hiddenSingle(): SudokuHint? {
        for ((unitIndex, unit) in units.withIndex()) for (digit in 1..9) {
            if (unit.any { values[it] == digit }) continue
            val places = unit.filter { digit in candidates[it] }
            if (places.size != 1) continue
            val cell = places.single()
            return SudokuHint(HintTechnique.HiddenSingle, cell, digit, true, listOf(
                HintStep("观察${unitName(unitIndex)}中数字 $digit 的可填位置。", unit.toSet()),
                HintStep("其他空格都被所在行、列或宫排除，只剩${location(cell)}。", setOf(cell)),
                HintStep("${location(cell)}可以填 $digit。", setOf(cell)),
            ))
        }
        return null
    }

    fun lockedCandidate(): SudokuHint? {
        // 在宫内只落在同一行或列，或在行列内只落在同一宫。
        for (sourceIndex in units.indices) for (digit in 1..9) {
            val source = units[sourceIndex]
            if (source.any { values[it] == digit }) continue
            val places = source.filter { digit in candidates[it] }
            if (places.size < 2) continue
            val targets = when {
                sourceIndex >= 18 -> listOfNotNull(
                    places.map { it / 9 }.distinct().singleOrNull()?.let { units[it] },
                    places.map { it % 9 }.distinct().singleOrNull()?.let { units[9 + it] },
                )
                sourceIndex < 18 -> places.map { (it / 27) * 3 + (it % 9 / 3) }.distinct()
                    .singleOrNull()?.let { listOf(units[18 + it]) }.orEmpty()
                else -> emptyList()
            }
            for (targetUnit in targets) {
                val eliminated = targetUnit.firstOrNull { it !in source && digit in candidates[it] } ?: continue
                return SudokuHint(HintTechnique.LockedCandidate, eliminated, digit, false, listOf(
                    HintStep("观察${unitName(sourceIndex)}中 $digit 的候选位置。", places.toSet()),
                    HintStep("这些位置都落在同一个交集内，$digit 必在这个交集内。", places.toSet()),
                    HintStep("因此${location(eliminated)}可排除候选数 $digit。", places.toSet() + eliminated),
                ))
            }
        }
        return null
    }

    fun nakedPair(): SudokuHint? {
        for ((unitIndex, unit) in units.withIndex()) {
            val pairs = unit.filter { candidates[it].size == 2 }.groupBy { candidates[it] }
            for ((digits, cells) in pairs) {
                if (cells.size != 2) continue
                val eliminated = unit.firstOrNull { it !in cells && candidates[it].any { digit -> digit in digits } } ?: continue
                val pairText = digits.sorted().joinToString("、")
                val eliminatedDigits = digits.filter { it in candidates[eliminated] }.sorted()
                return SudokuHint(HintTechnique.NakedPair, eliminated,
                    eliminatedDigits.first(), false, listOf(
                    HintStep("${unitName(unitIndex)}的${location(cells[0])}和${location(cells[1])}都只剩 $pairText。", cells.toSet()),
                    HintStep("这两个数字必定占据这两个格子，顺序暂时未知。", cells.toSet()),
                    HintStep("因此${location(eliminated)}可排除候选数 ${eliminatedDigits.joinToString("、")}。", cells.toSet() + eliminated),
                ))
            }
        }
        return null
    }

    fun hiddenPair(): SudokuHint? {
        for ((unitIndex, unit) in units.withIndex()) for (first in 1..8) for (second in first + 1..9) {
            val firstPlaces = unit.filter { first in candidates[it] }
            if (firstPlaces.size != 2 || firstPlaces != unit.filter { second in candidates[it] }) continue
            val eliminated = firstPlaces.firstOrNull { candidates[it].any { digit -> digit != first && digit != second } } ?: continue
            return SudokuHint(HintTechnique.HiddenPair, eliminated, candidates[eliminated].first { it != first && it != second }, false, listOf(
                HintStep("在${unitName(unitIndex)}里，$first 和 $second 都只可能出现在两个相同的格子。", firstPlaces.toSet()),
                HintStep("这两个格子必须分别放入 $first、$second。", firstPlaces.toSet()),
                HintStep("${location(eliminated)}里的其他候选数都可排除。", firstPlaces.toSet()),
            ))
        }
        return null
    }

    fun xWing(): SudokuHint? {
        for (digit in 1..9) for (byRows in listOf(true, false)) {
            val lines = (0..8).map { line ->
                (0..8).filter { position ->
                    digit in candidates[if (byRows) line * 9 + position else position * 9 + line]
                }
            }
            for (first in 0..7) for (second in first + 1..8) {
                if (lines[first].size != 2 || lines[first] != lines[second]) continue
                val cross = lines[first]
                val eliminated = (0..8).asSequence().filter { it != first && it != second }
                    .flatMap { line -> cross.asSequence().map { position ->
                        if (byRows) line * 9 + position else position * 9 + line
                    } }.firstOrNull { digit in candidates[it] } ?: continue
                val corners = setOf(
                    if (byRows) first * 9 + cross[0] else cross[0] * 9 + first,
                    if (byRows) first * 9 + cross[1] else cross[1] * 9 + first,
                    if (byRows) second * 9 + cross[0] else cross[0] * 9 + second,
                    if (byRows) second * 9 + cross[1] else cross[1] * 9 + second,
                )
                val axis = if (byRows) "行" else "列"
                val opposite = if (byRows) "列" else "行"
                return SudokuHint(HintTechnique.XWing, eliminated, digit, false, listOf(
                    HintStep("观察第${first + 1}${axis}与第${second + 1}${axis}的候选数 $digit。", corners),
                    HintStep("两条${axis}中，$digit 都只落在相同的两条${opposite}，形成四角。", corners),
                    HintStep("这两条${opposite}的其他格子不能填 $digit；${location(eliminated)}可排除它。", corners + eliminated),
                ))
            }
        }
        return null
    }

    fun xyWing(): SudokuHint? {
        val bivalue = (0..80).filter { candidates[it].size == 2 }
        for (pivot in bivalue) {
            val pivotDigits = candidates[pivot].sorted()
            val first = pivotDigits[0]
            val second = pivotDigits[1]
            for (wingA in bivalue.filter { it != pivot && sees(it, pivot) && first in candidates[it] && second !in candidates[it] }) {
                val shared = candidates[wingA].first { it != first }
                for (wingB in bivalue.filter { it != pivot && it != wingA && sees(it, pivot) && candidates[it] == setOf(second, shared) }) {
                    val eliminated = (0..80).firstOrNull {
                        it != pivot && it != wingA && it != wingB && shared in candidates[it] &&
                            sees(it, wingA) && sees(it, wingB)
                    } ?: continue
                    val pattern = setOf(pivot, wingA, wingB)
                    return SudokuHint(HintTechnique.XYWing, eliminated, shared, false, listOf(
                        HintStep("观察${location(pivot)}的候选数 $first、$second。", setOf(pivot)),
                        HintStep("它关联的两翼分别是${location(wingA)}的 $first、$shared 和${location(wingB)}的 $second、$shared；两种分支都会在一翼放入 $shared。", pattern),
                        HintStep("${location(eliminated)}同时看到两翼，因此可排除 $shared。", pattern + eliminated),
                    ))
                }
            }
        }
        return null
    }
}
