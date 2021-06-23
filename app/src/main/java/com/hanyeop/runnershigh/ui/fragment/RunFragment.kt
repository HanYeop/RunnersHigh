package com.hanyeop.runnershigh.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hanyeop.runnershigh.R
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hanyeop.runnershigh.databinding.FragmentRunBinding
import com.hanyeop.runnershigh.ui.activity.TrackingActivity
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

        // fab 클릭 시 Tracking 화면으로 이등
        binding.apply {
            runStartFab.setOnClickListener {
                val intent = Intent(activity,TrackingActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // 프래그먼트는 뷰보다 오래 지속 . 프래그먼트의 onDestroyView() 메서드에서 결합 클래스 인스턴스 참조를 정리
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}