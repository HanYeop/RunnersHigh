package com.hanyeop.runnershigh.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.databinding.FragmentStatisticsBinding
import com.hanyeop.runnershigh.util.TrackingUtility
import com.hanyeop.runnershigh.viewmodel.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    // 뷰모델 생성
    private val viewModel by viewModels<StatisticsViewModel>()

    // 참조 관리
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰바인딩
        _binding = FragmentStatisticsBinding.bind(view)
        subscribeToObservers()

    }

    // 통계 관찰
    private fun subscribeToObservers() {
        binding.apply {
            // 총 거리 계산
            viewModel.totalDistance.observe(viewLifecycleOwner) {
                it?.let {
                    val totalDistanceString = "${TrackingUtility.getFormattedDistance(it)}km"
                    totalDistanceText.text = totalDistanceString
                    totalDistanceText.isVisible = true
                    disProgressBar.isVisible = false
                }
            }

            // 총 시간 계산
            viewModel.totalTimeInMillis.observe(viewLifecycleOwner) {
                it?.let {
                    val totalTimeInMillis = TrackingUtility.getFormattedStopWatchTime(it)
                    totalTimeText.text = totalTimeInMillis
                    totalTimeText.isVisible = true
                    timeProgressBar.isVisible = false
                }
            }

            // 평균 속도 계산
            viewModel.totalAvgSpeed.observe(viewLifecycleOwner) {
                it?.let {
                    val roundedAvgSpeed = round(it * 10f) / 10f
                    val totalAvgSpeed = "${roundedAvgSpeed}km/h"
                    averageSpeedText.text = totalAvgSpeed
                    averageSpeedText.isVisible = true
                    avgProgressBar.isVisible = false
                }
            }

            // 총 칼로리 계산
            viewModel.totalCaloriesBurned.observe(viewLifecycleOwner) {
                it?.let {
                    val totalCaloriesBurned = "${it}kcal"
                    totalCaloriesText.text = totalCaloriesBurned
                    totalCaloriesText.isVisible = true
                    calProgressBar.isVisible = false
                }
            }

            // 최장 거리 계산
            viewModel.maxDistanceInMillis.observe(viewLifecycleOwner) {
                it?.let {
                    val maxDistanceString = "${TrackingUtility.getFormattedDistance(it)}km"
                    maxDistanceText.text = maxDistanceString
                    maxDistanceText.isVisible = true
                    maxDisProgressBar.isVisible = false
                }
            }

            // 최장 시간 계산
            viewModel.maxTimeInMillis.observe(viewLifecycleOwner) {
                it?.let {
                    val maxTimeInMillis = TrackingUtility.getFormattedStopWatchTime(it)
                    maxTimeText.text = maxTimeInMillis
                    maxTimeText.isVisible = true
                    maxTimeProgressBar.isVisible = false
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

