package com.rsschool.pomodoro.model

data class Stopwatch(
    val id: Int,
    val startTime:Long,
    var currentMs: Long,
    var isStarted: Boolean
)