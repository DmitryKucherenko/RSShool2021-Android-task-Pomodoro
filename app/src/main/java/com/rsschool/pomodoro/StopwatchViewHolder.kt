package com.rsschool.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.rsschool.pomodoro.databinding.StopwatchItemBinding
import com.rsschool.pomodoro.model.Stopwatch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {
    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {


        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        if(stopwatch.isStarted)
            setIsRecyclable(false)
        else if(!isRecyclable)
            setIsRecyclable(true)

        if(stopwatch.currentMs!=stopwatch.startTime)  binding.progressView.setCurrent(stopwatch.currentMs)else
            binding.progressView.setCurrent(0)
        if(stopwatch.isFinish) binding.root.setCardBackgroundColor(resources.getColor(R.color.pomodoroColorVariant))else
            binding.root.setCardBackgroundColor(resources.getColor(R.color.white))
        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {

        binding.startStopButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else
                listener.start(stopwatch.id)
            }


        binding.deleteButton.setOnClickListener {
            if(!isRecyclable)
                setIsRecyclable(true)
            binding.root.setCardBackgroundColor(resources.getColor(R.color.white))
            listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startStopButton.text="STOP"
        binding.root.setCardBackgroundColor(resources.getColor(R.color.white))
        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        binding.progressView.setPeriod(stopwatch.startTime)
        stopwatch.isFinish=false
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        binding.startStopButton.text="START"
        timer?.cancel()
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()

    }

        private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
            return object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_MS) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.stopwatchTimer.text = millisUntilFinished.displayTime()
                    stopwatch.currentMs=millisUntilFinished
                    binding.progressView.setCurrent(millisUntilFinished)
                }

                override fun onFinish() {
                    if(!isRecyclable)
                        setIsRecyclable(true)
                    stopTimer(stopwatch)
                    binding.progressView.setCurrent(0)
                    binding.stopwatchTimer.text = stopwatch.startTime.displayTime()
                    stopwatch.currentMs=stopwatch.startTime
                    stopwatch.isFinish=true
                    listener.stop(stopwatch.id, stopwatch.currentMs)
                    binding.root.setCardBackgroundColor(resources.getColor(R.color.pomodoroColorVariant))
                }
            }
        }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val START_TIME = "00:00:00"
        const val UNIT_TEN_MS = 10L
    }
}