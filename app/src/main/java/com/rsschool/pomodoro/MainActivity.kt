package com.rsschool.pomodoro

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rsschool.pomodoro.adapter.StopwatchAdapter
import com.rsschool.pomodoro.databinding.ActivityMainBinding
import com.rsschool.pomodoro.model.Stopwatch
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {
    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    //текущий активный id stopwatch
    private var currentId= -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }
        //добавляем слушателя на кнопку
        binding.addNewStopwatchButton.setOnClickListener {
            //получаем время таймера в минутах
            val startTime= binding.editTime.text.toString().toLongOrNull()?.times(60000L) ?: 0L
          //Чтобы приложение не зависло ограничиваем число таймеров
            if(stopwatches.size<=100){
                //добавляем в лист stopwatch
            stopwatches.add(Stopwatch(nextId++, startTime, currentMs = startTime, isStarted = false,false,null))
           //Передаем в адаптер лист stopwatch
            stopwatchAdapter.submitList(stopwatches.toList())}
            else Toast.makeText(applicationContext, "Timer count > max", LENGTH_LONG).show()
        }
    }

   //Передача id запущенного таймера и установка свойства isStarted=true
    override fun start(id: Int) {
        currentId=id
        changeStopwatch(id,null, true,null)
    }
    //Передача id остановленного таймера и установка свойства isStarted=false
    override fun stop(id: Int, currentMs: Long,isFinish:Boolean?) {
        if(id==currentId)currentId=-1
        changeStopwatch(id, currentMs, false,isFinish)
    }

    //удаление таймера из списка stopwatches по id и передача в адаптер обновленного списка
    override fun delete(id: Int) {
        if(id==currentId)currentId=-1
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }


//метод инициализации stopwatch для случаев start,stop,delete и передача в адаптер
    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean,isFinish:Boolean?) {
        stopwatches.replaceAll{
            when {
                it.id == id -> Stopwatch(it.id, it.startTime,currentMs ?: it.currentMs, isStarted,isFinish?:it.isFinish,it.timer)
                it.isStarted && isFinish==null->{
                    it.timer?.cancel()
                    Stopwatch(it.id,  it.startTime,currentMs ?: it.currentMs, false,isFinish?:it.isFinish,it.timer)
                    }
                else -> {it}
            }
        }
        stopwatchAdapter.submitList(stopwatches.toList())
    }



//запуск сервиса при уходе в background
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
            var startTime = stopwatchAdapter.currentList.find { it.id == currentId }?.currentMs ?: 0L
        if(startTime>0) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
            startService(startIntent)
        }
    }
//остановка сервиса при уходе в foreground
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        onAppForegrounded()
    }


    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Do your want out from application?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                exitProcess(0)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
            }.create().show()
    }


}