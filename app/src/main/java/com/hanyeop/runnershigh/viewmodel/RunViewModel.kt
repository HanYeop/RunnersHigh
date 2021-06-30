package com.hanyeop.runnershigh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanyeop.runnershigh.model.Run
import com.hanyeop.runnershigh.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    val runsSortedByDate = runRepository.getAllRunsSortedByDate()
}