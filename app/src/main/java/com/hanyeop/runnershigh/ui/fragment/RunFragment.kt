package com.hanyeop.runnershigh.ui.fragment

import androidx.fragment.app.Fragment
import com.hanyeop.runnershigh.R
import androidx.fragment.app.viewModels
import com.hanyeop.runnershigh.viewmodel.RunViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    // 뷰모델 생성
    private val viewModel by viewModels<RunViewModel>()


}