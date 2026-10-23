package com.zxq.sudoku

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform