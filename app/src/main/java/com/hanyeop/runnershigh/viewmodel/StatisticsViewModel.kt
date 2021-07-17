package com.hanyeop.runnershigh.viewmodel

import androidx.lifecycle.ViewModel
import com.hanyeop.runnershigh.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val runRepository: RunRepository
) : ViewModel() {

    var totalDistance = runRepository.getTotalDistance()
    var totalTimeInMillis = runRepository.getTotalTimeInMillis()
    var totalAvgSpeed = runRepository.getTotalAvgSpeed()
    var totalCaloriesBurned = runRepository.getTotalCaloriesBurned()
    var maxTimeInMillis = runRepository.getMaxTimeInMillis()
    var maxDistanceInMillis = runRepository.getMaxDistanceInMillis()
    var totalRunning = runRepository.getTotalRunning()
}