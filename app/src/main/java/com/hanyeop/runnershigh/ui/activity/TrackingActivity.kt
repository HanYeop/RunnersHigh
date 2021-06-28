package com.hanyeop.runnershigh.ui.activity

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.databinding.ActivityTrackingBinding
import com.hanyeop.runnershigh.service.Polyline
import com.hanyeop.runnershigh.service.TrackingService
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_PAUSE_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_START_OR_RESUME_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_STOP_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.MAP_ZOOM
import com.hanyeop.runnershigh.util.Constants.Companion.POLYLINE_COLOR
import com.hanyeop.runnershigh.util.Constants.Companion.POLYLINE_WIDTH
import com.hanyeop.runnershigh.util.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingActivity : AppCompatActivity() {

    // ActivityTrackingBinding 선언
    private lateinit var binding : ActivityTrackingBinding

    // 구글맵 선언
    private var map: GoogleMap? = null

    // 라이브 데이터를 받아온 값들
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var currentTimeInMillis = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰바인딩
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            mapView.onCreate(savedInstanceState)
            // 맵 불러오기
            mapView.getMapAsync {
                map = it
                /**
                 * 위치 처음에 줌하는거 구현해야함
                 */
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
        })

        // 시간(타이머) 경과 관찰
        TrackingService.timeRunInMillis.observe(this, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it, true)
            binding.timerText.text = formattedTime
        })
    }

    // 위치 추적 상태에 따른 레이아웃 변경
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        binding.apply {
            if (!isTracking) {
            startButton.text = "다시 시작하기"
            finishButton.visibility = View.VISIBLE
            }
            else if (isTracking) {
                startButton.text = "정지하기"
                finishButton.visibility = View.GONE
            }
        }
    }

    // 지도 위치 이동
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
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
        }
    }

    // 서비스에게 명령을 전달함
    private fun sendCommandToService(action : String) =
        Intent(this,TrackingService::class.java).also {
            it.action = action
            this.startService(it)
        }

    // 저장하지 않고 종료
    private fun stopRun() {
        binding.timerText.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        finish()
    }

    // 뒤로가기 버튼 눌렀을 때
    override fun onBackPressed() {
        if(currentTimeInMillis > 0L){
            // 잠시 정지시킴
            sendCommandToService(ACTION_PAUSE_SERVICE)
            var builder = AlertDialog.Builder(this)
            builder.setTitle("달리기를 취소할까요? 기록은 저장되지 않습니다.")
                .setPositiveButton("네"){ _,_ ->
                    // 달리기 종료시킴 (저장X)
                    stopRun()
                }
                .setNegativeButton("아니오"){_,_ ->
                    // 다시 시작
                    sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
                }.create()
            builder.show()
        }
        else{
            super.onBackPressed()
        }
    }

    /**
     * 라이프 사이클에 맞게 맵뷰를 처리해줌
     */
    override fun onResume() {
        binding.mapView.onResume()
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