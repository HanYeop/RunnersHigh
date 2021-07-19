package com.hanyeop.runnershigh.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanyeop.runnershigh.model.Run
import com.hanyeop.runnershigh.repository.RunRepository
import com.hanyeop.runnershigh.util.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunViewModel @Inject constructor(
    private val runRepository: RunRepository
) : ViewModel() {

    fun insertRun(run : Run) = viewModelScope.launch {
        runRepository.insertRun(run)
    }

    fun deleteRun(run : Run) = viewModelScope.launch {
        runRepository.deleteRun(run)
    }

    fun deleteAllRun() = viewModelScope.launch(Dispatchers.IO) {
        runRepository.deleteAllRun()
    }

    fun updateRun(run : Run) = viewModelScope.launch(Dispatchers.IO) {
        runRepository.updateRun(run)
    }

    private val runsSortedByDate = runRepository.getAllRunsSortedByDate()
    private val runsSortedByTimeInMillis = runRepository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByDistance = runRepository.getAllRunsSortedByDistance()
    private val runsSortedByAvgSpeed = runRepository.getAllRunsSortedByAvgSpeed()
    private val runsSortedByCaloriesBurned = runRepository.getAllRunsSortedByCaloriesBurned()
    private fun monthRunsSortedByDate(year :Int, month:Int) = runRepository.getMonthRunsSortedByDate(year,month)

    // 라이브데이터 묶어줌 (단일 옵저버에 추가 가능)
    val runs = MediatorLiveData<List<Run>>()

    // 정렬 기준 (기본 값 날짜 순)
    var sortType = SortType.DATE

    // 변경된 타입에 따라 데이터를 변경
    init {
        runs.addSource(runsSortedByDate) { result ->
            if(sortType == SortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByTimeInMillis) { result ->
            if(sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDistance) { result ->
            if(sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByAvgSpeed) { result ->
            if(sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByCaloriesBurned) { result ->
            if(sortType == SortType.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
    }

    // 스피너에서 선택된 타입으로 바꿈
    fun sortRuns(sortType: SortType) = when(sortType) {
        SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortedByTimeInMillis.value?.let { runs.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }
}