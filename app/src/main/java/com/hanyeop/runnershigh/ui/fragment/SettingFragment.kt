package com.hanyeop.runnershigh.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.databinding.FragmentSettingBinding
import com.hanyeop.runnershigh.viewmodel.RunViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment(R.layout.fragment_setting) {

    // 뷰모델 생성
    private val viewModel by viewModels<RunViewModel>()

    // 참조 관리
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 뷰바인딩
        _binding = FragmentSettingBinding.bind(view)

        binding.apply{
            allDeleteButton.setOnClickListener {
                viewModel.deleteAllRun()
            }
        }
    }
}