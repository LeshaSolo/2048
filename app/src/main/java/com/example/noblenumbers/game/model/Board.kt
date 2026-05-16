package com.example.noblenumbers.game.model

data class Board(
    val size: Int = 5,
    val tiles: List<Tile> = emptyList(),
) {
    init {
        require(size == 5) { "Noble Numbers uses a fixed 5x5 board." }
        require(tiles.all { it.row in 0 until size && it.column in 0 until size })
    }

    fun tileAt(row: Int, column: Int): Tile? = tiles.firstOrNull { it.row == row && it.column == column }

    fun emptyCells(): List<Cell> {
        val occupied = tiles.map { Cell(it.row, it.column) }.toSet()
        return (0 until size).flatMap { row ->
            (0 until size).map { column -> Cell(row, column) }
        }.filterNot { it in occupied }
    }

    fun maxTileValue(): Int = tiles.maxOfOrNull { it.value } ?: 0
}

data class Cell(
    val row: Int,
    val column: Int,
)
