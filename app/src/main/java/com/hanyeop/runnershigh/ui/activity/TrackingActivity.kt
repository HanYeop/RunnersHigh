package com.hanyeop.runnershigh.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.hanyeop.runnershigh.databinding.ActivityTrackingBinding
import com.hanyeop.runnershigh.model.Run
import com.hanyeop.runnershigh.service.Polyline
import com.hanyeop.runnershigh.service.TrackingService
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_PAUSE_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_START_OR_RESUME_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_STOP_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_COLOR
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_WEIGHT
import com.hanyeop.runnershigh.util.Constants.Companion.MAP_ZOOM
import com.hanyeop.runnershigh.util.Constants.Companion.POLYLINE_WIDTH
import com.hanyeop.runnershigh.util.TrackingUtility
import com.hanyeop.runnershigh.viewmodel.RunViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingActivity : AppCompatActivity() {

    // ActivityTrackingBinding 선언
    private lateinit var binding : ActivityTrackingBinding

    // 뷰모델 생성
    private val viewModel by viewModels<RunViewModel>()

    // 구글맵 선언
    private var map: GoogleMap? = null

    // 라이브 데이터를 받아온 값들
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var currentTimeInMillis = 0L

    // SharedPreferences 주입
    @Inject
    lateinit var sharedPref: SharedPreferences

    // 총 이동거리
    private var sumDistance = 0f

    // 선 색상
    private var POLYLINE_COLOR = Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰바인딩
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 색상 정보 불러오기
        colorLoad()

        binding.apply {
            mapView.onCreate(savedInstanceState)
            // 맵 불러오기
            mapView.getMapAsync {
                map = it
                // 알림 클릭 등으로 다시 생성되었을 때 경로 표시
                addAllPolylines()
                moveCameraToUser()

                // 거리 텍스트 변경 ( 알림 클릭 등으로 다시 생성되었을 때 초기화 방지)
                sumDistance = updateDistance()
                binding.distanceText.text = "${TrackingUtility.getFormattedDistance(sumDistance)}Km"
            }

            // 알림창에서 불러 왔을 때 현재 레이아웃 불러오기
            if(TrackingService.isTracking.value != null){
                currentTimeInMillis = TrackingService.timeRunInMillis.value!!
                updateTracking(TrackingService.isTracking.value!!)
            }

            // 스타트 버튼 클릭 시 서비스를 시작함
            startButton.setOnClickListener {
                // 이미 실행 중이면 일시 정지
                if(isTracking) {
                    sendCommandToService(ACTION_PAUSE_SERVICE)
                }
                // 아니라면 실행
                else{
                    sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
                }
            }

            // 종료 버튼 클릭 시 저장하고 종료
            finishButton.setOnClickListener {
                zoomToWholeTrack()
                endRunAndSaveToDB()
            }
        }

        // 위치 추적 여부 관찰하여 updateTracking 호출
        TrackingService.isTracking.observe(this, Observer {
            updateTracking(it)
        })

        // 경로 변경 관찰
        TrackingService.pathPoints.observe(this, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()

            // 거리 텍스트 변경
            binding.distanceText.text = "${TrackingUtility.getFormattedDistance(sumDistance)}Km"
        })

        // 시간(타이머) 경과 관찰
        TrackingService.timeRunInMillis.observe(this, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it, true)
            binding.timerText.text = formattedTime
        })
    }

    // 스냅샷 찍기 위하여 전체 경로가 다 보이게 줌
    private fun zoomToWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (point in polyline) {
                bounds.include(point)
            }
        }
        val width = binding.mapView.width
        val height = binding.mapView.height
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                width,
                height,
                (height * 0.05f).toInt()
            )
        )
    }

    // 달리기 기록 저장
    private fun endRunAndSaveToDB() {
        // 몸무게 불러오기
        val weight = sharedPref.getFloat(KEY_WEIGHT,70f)

        /**
         * 날짜 변환
         */
        val calendar = Calendar.getInstance()
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())

        val year = yearFormat.format(calendar.time)
        val month = monthFormat.format(calendar.time)
        val day = dayFormat.format(calendar.time)
        val title = "${year}년 ${month}월 ${day}일 러닝"

        map?.snapshot { bmp ->
            // 반올림
            val avgSpeed =
                round((sumDistance / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val timestamp = calendar.timeInMillis
            val caloriesBurned = ((sumDistance / 1000f) * weight).toInt()
            val run = Run(0,bmp,timestamp,avgSpeed,sumDistance,currentTimeInMillis,
                caloriesBurned,title,year.toInt(),month.toInt(),day.toInt() )
            viewModel.insertRun(run)
            Toast.makeText(this, "달리기 기록이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            stopRun()
        }
    }

    // 위치 추적 상태에 따른 레이아웃 변경
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        binding.apply {
            if (!isTracking && currentTimeInMillis > 0L) {
                startButton.text = "다시 시작하기"
                finishButton.visibility = View.VISIBLE
            }
            else if (isTracking) {
                startButton.text = "정지하기"
                finishButton.visibility = View.GONE
            }
        }
    }

    // 경로 전부 표시
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    // 지도 위치 이동
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    // 총 이동거리
    private fun updateDistance() : Float{
        var distanceInMeters = 0f
        for (polyline in pathPoints) {
            distanceInMeters += calculatePolylineLength(polyline)
        }
        return distanceInMeters
    }

    // 총 이동거리 계산
    private fun calculatePolylineLength(polyline: Polyline): Float {
        var distance = 0f
        // 두 경로 사이마다 거리를 계산하여 합함
        for (i in 0 until polyline.size - 1) {
            val pos1 = polyline[i]
            val pos2 = polyline[i + 1]
            val result = FloatArray(1)
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            distance += result[0]
        }
        return distance
    }

    // 경로 표시 (마지막 전, 마지막 경로 연결)
    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2] // 마지막 전 경로
            val lastLatLng = pathPoints.last().last() // 마지막 경로
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)

            // 이동거리 계산
            val result = FloatArray(1)
            Location.distanceBetween(
                preLastLatLng.latitude,
                preLastLatLng.longitude,
                lastLatLng.latitude,
                lastLatLng.longitude,
                result
            )
            sumDistance += result[0]
        }
    }

    // 서비스에게 명령을 전달함
    private fun sendCommandToService(action : String) =
        Intent(this,TrackingService::class.java).also {
            it.action = action
            this.startService(it)
        }

    // 달리기 종료
    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        finish()
    }

    // 뒤로가기 버튼 눌렀을 때
    override fun onBackPressed() {
        if(currentTimeInMillis > 0L){
            var builder = AlertDialog.Builder(this)
            builder.setTitle("달리기를 취소할까요? 기록은 저장되지 않습니다.")
                .setPositiveButton("네"){ _,_ ->
                    // 달리기 종료시킴 (저장X)
                    stopRun()
                }
                .setNegativeButton("아니오"){_,_ ->
                    // 다시 시작
                }.create()
            builder.show()
        }
        else{
            super.onBackPressed()
        }
    }

    // 색상 정보 불러오기
    private fun colorLoad(){
        val colorState = sharedPref.getInt(KEY_COLOR,1)
        when (colorState){
            1 -> POLYLINE_COLOR = Color.RED
            2 -> POLYLINE_COLOR = Color.BLUE
            3 -> POLYLINE_COLOR = Color.GREEN
            4 -> POLYLINE_COLOR = Color.BLACK
        }
    }

    /**
     * 라이프 사이클에 맞게 맵뷰를 처리해줌
     */
    override fun onResume() {
        binding.mapView.onResume()
        // 백그라운드 상태에서 돌아왔을 때 경로 표시
        addAllPolylines()
        moveCameraToUser()

        // 거리 텍스트 동기화
        sumDistance = updateDistance()
        binding.distanceText.text = "${TrackingUtility.getFormattedDistance(sumDistance)}Km"
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}