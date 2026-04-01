package ru.netology.nmedia.util

import java.util.concurrent.atomic.AtomicLong

object LocalId {
    private val next = AtomicLong(-1L)
    fun nextId(): Long = next.getAndDecrement()
}
