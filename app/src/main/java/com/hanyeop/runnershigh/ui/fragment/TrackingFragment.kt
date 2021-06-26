package com.hanyeop.runnershigh.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.databinding.FragmentTrackingBinding
import com.hanyeop.runnershigh.service.TrackingService
import com.hanyeop.runnershigh.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    // 참조 관리
    private var _binding : FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    // 구글맵 선언
    private var map: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰바인딩
        _binding = FragmentTrackingBinding.bind(view)

        binding.apply {
            mapView.onCreate(savedInstanceState)
            // 맵 불러오기
            mapView.getMapAsync {
                map = it
            }

            // 스타트 버튼 클릭 시 서비스를 시작함
            startButton.setOnClickListener {
                sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
            }
        }
    }

    // 서비스에게 명령을 전달함
    private fun sendCommandToService(action : String) =
        Intent(requireActivity(), TrackingService::class.java).also {
            it.action = action
            requireActivity().startService(it)
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