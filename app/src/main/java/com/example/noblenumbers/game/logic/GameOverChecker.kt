package com.example.noblenumbers.game.logic

import com.example.noblenumbers.game.model.Board

class GameOverChecker {
    fun isGameOver(board: Board): Boolean {
        if (board.emptyCells().isNotEmpty()) return false

        for (row in 0 until board.size) {
            for (column in 0 until board.size) {
                val current = board.tileAt(row, column) ?: continue
                val right = board.tileAt(row, column + 1)
                val down = board.tileAt(row + 1, column)
                if (right?.value == current.value || down?.value == current.value) return false
            }
        }
        return true
    }
}
