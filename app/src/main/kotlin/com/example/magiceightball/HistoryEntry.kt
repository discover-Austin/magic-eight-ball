package com.example.magiceightball

/** A single question–response pair recorded in the session history. */
data class HistoryEntry(
    val question: String,
    val response: EightBall.Response,
    val timestamp: Long = System.currentTimeMillis()
)
