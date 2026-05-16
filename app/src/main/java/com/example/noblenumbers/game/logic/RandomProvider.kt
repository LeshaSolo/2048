package com.example.noblenumbers.game.logic

import kotlin.random.Random

interface RandomProvider {
    fun nextInt(bound: Int): Int
    fun nextFloat(): Float
}

class KotlinRandomProvider(
    private val random: Random = Random.Default,
) : RandomProvider {
    override fun nextInt(bound: Int): Int = random.nextInt(bound)
    override fun nextFloat(): Float = random.nextFloat()
}
