package com.rsschool.pomodoro

interface StopwatchListener {
    fun start(id: Int)
    fun stop(id: Int, currentMs: Long,isFinish:Boolean?)
    fun delete(id: Int)
}