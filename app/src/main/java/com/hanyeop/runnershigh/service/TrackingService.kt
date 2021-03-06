package com.hanyeop.runnershigh.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_PAUSE_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_START_OR_RESUME_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_STOP_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.FASTEST_LOCATION_UPDATE_INTERVAL
import com.hanyeop.runnershigh.util.Constants.Companion.LOCATION_UPDATE_INTERVAL
import com.hanyeop.runnershigh.util.Constants.Companion.NOTIFICATION_CHANNEL_ID
import com.hanyeop.runnershigh.util.Constants.Companion.NOTIFICATION_CHANNEL_NAME
import com.hanyeop.runnershigh.util.Constants.Companion.NOTIFICATION_ID
import com.hanyeop.runnershigh.util.Constants.Companion.TAG
import com.hanyeop.runnershigh.util.Constants.Companion.TIMER_UPDATE_INTERVAL
import com.hanyeop.runnershigh.util.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    // ????????? ?????? ??????
    private var serviceKilled = false

    // FusedLocationProviderClient ??????
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // NotificationCompat.Builder ??????
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    // NotificationCompat.Builder ???????????? ??????
    lateinit var currentNotificationBuilder : NotificationCompat.Builder

    // ???????????? ????????? ??????
    private val timeRunInSeconds = MutableLiveData<Long>()

    private var isTimerEnabled = false // ????????? ?????? ??????
    private var lapTime = 0L // ?????? ??? ????????? ??????
    private var totalTime = 0L // ?????? ??? ???????????? ??????
    private var timeStarted = 0L // ?????? ????????? ??????
    private var lastSecondTimestamp = 0L // 1??? ?????? ????????? ??????


    companion object{
        val isTracking = MutableLiveData<Boolean>() // ?????? ?????? ?????? ??????
        val pathPoints = MutableLiveData<Polylines>() // LatLng = ??????,??????
        val timeRunInMillis = MutableLiveData<Long>() // ?????? ????????? ??????
        var isFirstRun = false // ?????? ?????? ?????? (false = ??????????????????)
    }

    // ?????????
    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()

        // ?????? ?????? ????????? ?????? ???????????? ??????
        isTracking.observe(this, Observer {
            updateLocation(it)
            updateNotificationTrackingState(it)
        })
    }

    // ???????????? ?????? ????????? ???
    private fun killService(){
        serviceKilled = true
        isFirstRun = false
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    // ????????? ?????? ??????, ?????? ??????
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "????????????" else "?????? ????????????"
        // ?????? or ?????? ?????? ?????? ??? ?????? ?????? ?????? ?????????
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        // ????????? ??????????????? ?????? ???
        if(!serviceKilled) {
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(
                    R.drawable.ic_baseline_directions_run_24,
                    notificationActionText,
                    pendingIntent
                )
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    // ????????? ??????
    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            // ?????? ?????? ????????? ???
            while (isTracking.value!!){
                // ?????? ?????? - ?????? ?????? => ????????? ??????
                lapTime = System.currentTimeMillis() - timeStarted
                // ????????? (??????????????? ????????? ??????) + ???????????? ??????
                timeRunInMillis.postValue(totalTime + lapTime)
                // ???????????? ????????? ?????? ??? ????????? ?????????
                if(timeRunInMillis.value!! >= lastSecondTimestamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            // ?????? ????????? ??????(??????) ????????? ??? ????????? ??????
            totalTime += lapTime
        }
    }

    // ??? polyline ??????
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf())) // null ????????? ?????????

    // ???????????? ??????
    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    // ???????????? ???????????? addPathPoint ??? ??????
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result?.locations?.let { locations ->
                    for(location in locations) {
                        addPathPoint(location)
                        Log.d(TAG, "${location.latitude} , ${location.longitude}")
                    }
                }
            }
        }
    }

    // ?????? ?????? ????????????
    @SuppressLint("MissingPermission")
    private fun updateLocation(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL // ?????? ???????????? ??????
                    fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL // ?????? ?????? ?????? ???????????? ??????
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY // ?????????????????? ???????????? ????????? ???????????? ??????????????? ??????
                    maxWaitTime = LOCATION_UPDATE_INTERVAL // ?????? ????????????
                }
                fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    // ???????????? ?????? ????????? ???
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{
            when(it.action){
                // ??????, ?????? ????????? ???
                ACTION_START_OR_RESUME_SERVICE ->{
                    if(!isFirstRun){
                        Log.d(TAG, "????????? ")
                        startForegroundService()
                        isFirstRun = true
                    }else{
                        Log.d(TAG, "????????? ")
                        startTimer()
                    }
                }
                // ?????? ????????? ???
                ACTION_PAUSE_SERVICE ->{
                    Log.d(TAG, "?????? ")
                    pauseService()
                }
                // ?????? ????????? ???
                ACTION_STOP_SERVICE ->{
                    Log.d(TAG, "?????? ")
                    killService()
                }
                else -> null
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // ????????? ??????
   private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    // Notification ??????, ????????? ??????
    private fun startForegroundService(){
//        addEmptyPolyline()
//        isTracking.postValue(true)
        startTimer()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())

        // ?????? ??????????????? ???????????? ?????? ??????
        timeRunInSeconds.observe(this, Observer {
            // ????????? ??????????????? ?????? ???
            if(!serviceKilled) {
                val notification = currentNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    // ?????? ?????????
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW // ????????? ??????
        )
        notificationManager.createNotificationChannel(channel)
    }
}