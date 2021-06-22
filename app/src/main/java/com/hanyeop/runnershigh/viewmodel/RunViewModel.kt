package com.hanyeop.runnershigh.viewmodel

import androidx.lifecycle.ViewModel
import com.hanyeop.runnershigh.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RunViewModel @Inject constructor(
    private val runRepository: RunRepository
) : ViewModel() {

}