package com.zxq.sudoku.collection

import androidx.compose.ui.graphics.Color

internal enum class CollectionGame(val label: String, val description: String, val badge: String, val color: Color) {
    Sudoku("数独", "数独小岛 ，经典与对战", "9", Color(0xFFFFEBC5)),
    ChineseChess("中国象棋", "楚河汉界，落子有方", "象", Color(0xFFFFDCD2)),
    Gomoku("五子棋", "连成五子，先下一城", "五", Color(0xFFE6E5FA)),
    Go("围棋", "方寸之间，围地成势", "围", Color(0xFFDCEDE9)),
    Game2048("2048", "合并数字，挑战更高分", "2048", Color(0xFFFFE5B5)),
}
