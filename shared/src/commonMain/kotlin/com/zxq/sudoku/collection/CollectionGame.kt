package com.zxq.sudoku.collection

import androidx.compose.ui.graphics.Color

internal enum class CollectionGame(val label: String, val description: String, val badge: String, val color: Color) {
    Sudoku("数独", "数独小岛 ，经典与对战", "9", Color(0xFFFFEBC5)),
    Tetris("俄罗斯方块", "旋转方块，消除整行", "方", Color(0xFFE8DFF5)),
    ChineseChess("中国象棋", "楚河汉界，落子有方", "象", Color(0xFFFFDCD2)),
    Gomoku("五子棋", "连成五子，先下一城", "五", Color(0xFFE6E5FA)),
    Go("围棋", "方寸之间，围地成势", "围", Color(0xFFDCEDE9)),
    Game2048("2048", "合并数字，挑战更高分", "2048", Color(0xFFFFE5B5)),
    InternationalChess("国际象棋", "棋盘之上，巧布攻防", "棋", Color(0xFFDCE8F5)),
    Klotski("华容道", "挪动方块，巧解迷局", "华", Color(0xFFF5E3D6)),
    Sokoban("推箱子", "规划路线，推向目标", "箱", Color(0xFFE3EFD7)),
    Minesweeper("扫雷", "找出地雷，揭开安全区域", "雷", Color(0xFFFFE5D9)),
    Maze("迷宫", "寻找路径，走出迷宫", "迷", Color(0xFFE2EAF8)),
}
