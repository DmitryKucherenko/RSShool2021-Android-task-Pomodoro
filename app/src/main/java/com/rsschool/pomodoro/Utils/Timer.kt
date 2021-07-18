package com.rsschool.pomodoro

import android.os.CountDownTimer
import com.rsschool.pomodoro.model.Stopwatch


private const val UNIT_TEN_MS = 10L

fun timer(stopwatch: Stopwatch, tick: (Long) -> Unit, finish: () -> Unit): CountDownTimer {
    return object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_MS) {

        override fun onTick(millisUntilFinished: Long) {
            tick(millisUntilFinished)
        }

        override fun onFinish() {
            finish()
        }
    }
}



