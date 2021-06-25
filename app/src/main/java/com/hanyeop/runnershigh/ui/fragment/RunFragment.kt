package com.hanyeop.runnershigh.ui.fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hanyeop.runnershigh.R
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hanyeop.runnershigh.databinding.FragmentRunBinding
import com.hanyeop.runnershigh.util.Constants.Companion.TAG
import com.hanyeop.runnershigh.viewmodel.RunViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    // 뷰모델 생성
    private val viewModel by viewModels<RunViewModel>()

    // 참조 관리
    private var _binding : FragmentRunBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰바인딩
        _binding = FragmentRunBinding.bind(view)

        binding.apply {

            // fab 클릭 시 Tracking 화면으로 이등
            runStartFab.setOnClickListener {

                // 권한 체크해서 권한이 있을 때
                if(ContextCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){

                    findNavController().navigate(R.id.action_runFragment_to_trackingActivity)
                }

                // 권한이 없을 때 권한을 요구함
                else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        1
                    )
                }
            }
        }
    }

    // 프래그먼트는 뷰보다 오래 지속 . 프래그먼트의 onDestroyView() 메서드에서 결합 클래스 인스턴스 참조를 정리
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}