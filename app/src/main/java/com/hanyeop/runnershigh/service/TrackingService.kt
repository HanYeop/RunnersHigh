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
import com.hanyeop.runnershigh.ui.activity.MainActivity
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_PAUSE_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_SHOW_TRACKING_ACTIVITY
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    // 처음 실행 여부
    private var isFirstRun = false

    // FusedLocationProviderClient 선언
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // 알림창에 표시될 시간
    private val timeRunInSeconds = MutableLiveData<Long>()

    private var isTimerEnabled = false // 타이머 실행 여부
    private var lapTime = 0L // 시작 후 측정한 시간
    private var totalTime = 0L // 정지 시 저장되는 시간
    private var timeStarted = 0L // 측정 시작된 시간
    private var lastSecondTimestamp = 0L // 1초 단위 체크를 위함


    companion object{
        val isTracking = MutableLiveData<Boolean>() // 위치 추적 상태 여부
        val pathPoints = MutableLiveData<Polylines>() // LatLng = 위도,경도
        val timeRunInMillis = MutableLiveData<Long>() // 뷰에 표시될 시간
    }

    // 초기화
    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        // 위치 추적 상태가 되면 업데이트 호출
        isTracking.observe(this, Observer {
            updateLocation(it)
        })
    }

    // 타이머 시작
    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            // 위치 추적 상태일 때
            while (isTracking.value!!){
                // 현재 시간 - 시작 시간 => 경과한 시간
                lapTime = System.currentTimeMillis() - timeStarted
                // 총시간 (일시정지시 저장된 시간) + 경과시간 전달
                timeRunInMillis.postValue(totalTime + lapTime)
                // 알림창에 표시될 시간 초 단위로 계산함
                if(timeRunInMillis.value!! >= lastSecondTimestamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            // 위치 추적이 종료되었을 때 총시간 저장
            totalTime += lapTime
        }
    }

    // 빈 polyline 추가
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf())) // null 이라면 초기화

    // 위치정보 추가
    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    // 위치정보 수신하여 addPathPoint 로 추가
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

    // 위치 정보 요청하기
    @SuppressLint("MissingPermission")
    private fun updateLocation(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL // 위치 업데이트 주기
                    fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL // 가장 빠른 위치 업데이트 주기
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY // 배터리소모를 고려하지 않으며 정확도를 최우선으로 고려
                    maxWaitTime = LOCATION_UPDATE_INTERVAL // 최대 대기시간
                }
                fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    // 서비스가 호출 되었을 때
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{
            when(it.action){
                // 시작, 재개 되었을 때
                ACTION_START_OR_RESUME_SERVICE ->{
                    if(!isFirstRun){
                        Log.d(TAG, "시작함 ")
                        startForegroundService()
                        isFirstRun = true
                    }else{
                        Log.d(TAG, "실행중 ")
                        startTimer()
                    }
                }
                // 중지 되었을 때
                ACTION_PAUSE_SERVICE ->{
                    Log.d(TAG, "중지 ")
                    pauseService()
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

    // 서비스 일시정지
   private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    // Notification 등록, 서비스 시작
    private fun startForegroundService(){
//        addEmptyPolyline()
//        isTracking.postValue(true)
        startTimer()

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

    // 알림 클릭 시 이동할 액티비티
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