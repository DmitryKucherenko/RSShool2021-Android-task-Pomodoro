package com.rsschool.pomodoro

interface StopwatchListener {
    fun start(id: Int)
    fun stop(id: Int, currentMs: Long)
    fun delete(id: Int)
}