package com.example.snaplog

data class Entry(
    val id: String = "",
    val caption: String = "",
    val quote: String = "",
    val imageUrl: String = "",
    val createdAt: Long = 0L
)
