package com.rsschool.pomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {
//флаг, определяет запущен ли сервис или нет, чтобы не стартовать повторно
    private var isServiceStarted = false
    //мы будем обращаться к NotificationManager, когда нам нужно показать нотификацию или обновить её состояние.
    private var notificationManager: NotificationManager? = null
    //сслыка на таймер
    private var timer: CountDownTimer? = null
//Создаем Notification Builder
    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro")
            .setGroup("Timer")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())  //действие при нажатии
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
    }

    override fun onCreate() {
        super.onCreate()
        //Создаем Notification Manager.Когда нам нужно показать нотификацию или обновить её состояние. Это системный класс, мы можем влиять на отображение нотификаций только через него.
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }
//обрабатываем Intent. Этот метод вызывается когда сервис запускается. Мы будем передавать параметры для запуска и остановки сервиса через Intent.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
//получаем данные из Intent и определяем что делаем дальше: стартуем или останавливаем сервис.
    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                val startTime = intent?.extras?.getLong(STARTED_TIMER_TIME_MS) ?: return
                commandStart(startTime)
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }

    //Стартуем сервис, если он еще не запущен, создаем нотификацию
    private fun commandStart(startTime: Long) {
        if (isServiceStarted) {
            return
        }
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer(startTime)
        } finally {
            isServiceStarted = true
        }
    }
//продолжаем отсчитывать секундомер.
    private fun continueTimer(startTime: Long) {
        timer = getTimer(
            startTime,
            interval = SECOND,
            tick={ notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(it.displayTime())
                )},
            finish={
                //commandStop()
            })
            .start()
          }
//останавливаем обновление секундомера timer?.cancel(), убираем сервис из форегроунд стейта stopForeground(true), и останавливаем сервис stopSelf()
    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        try {
            timer?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }
//вызываем startForegroundService() или startService() в зависимости от текущего API.
    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            startService(Intent(this, ForegroundService::class.java))
        }
    }
//создаем канал, если API >= Android O. Создаем нотификацию и вызываем startForeground()
    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("Pomodoro timer")
        startForeground(NOTIFICATION_ID, notification)
    }

    //получаем нотификацию
    private fun getNotification(content: String) = builder.setContentText(content).build()

//Создаем канал
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }
//Действие при нажатии на нофикацию: Вызываем активити
    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private companion object {
        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
    }
}