package com.hanyeop.runnershigh.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.ui.activity.MainActivity
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_PAUSE_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_SHOW_TRACKING_ACTIVITY
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_START_OR_RESUME_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_STOP_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.NOTIFICATION_CHANNEL_ID
import com.hanyeop.runnershigh.util.Constants.Companion.NOTIFICATION_CHANNEL_NAME
import com.hanyeop.runnershigh.util.Constants.Companion.NOTIFICATION_ID
import com.hanyeop.runnershigh.util.Constants.Companion.TAG

class TrackingService : LifecycleService() {

    private var running = false

    // 서비스가 호출 되었을 때
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{
            when(it.action){
                // 시작, 재개 되었을 때
                ACTION_START_OR_RESUME_SERVICE ->{
                    if(!running){
                        Log.d(TAG, "시작함 ")
                        startForegroundService()
                        running = true
                    }else{
                        Log.d(TAG, "실행중 ")
                    }
                }
                // 중지 되었을 때
                ACTION_PAUSE_SERVICE ->{
                    Log.d(TAG, "중지 ")
                }
                // 종료 되었을 때
                ACTION_STOP_SERVICE ->{
                    Log.d(TAG, "종료 ")
                }
                else -> null
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
            .setContentTitle("Runner's High!")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,0,
        Intent(this,MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_ACTIVITY
        }, PendingIntent.FLAG_UPDATE_CURRENT
    )

    // 채널 만들기
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW // 알림음 없음
        )
        notificationManager.createNotificationChannel(channel)
    }
}