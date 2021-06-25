package com.hanyeop.runnershigh.service

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_PAUSE_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_START_OR_RESUME_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_STOP_SERVICE
import com.hanyeop.runnershigh.util.Constants.Companion.TAG

class TrackingService : LifecycleService() {

    // 서비스가 호출 되었을 때
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{
            when(it.action){
                // 시작, 재개 되었을 때
                ACTION_START_OR_RESUME_SERVICE ->{
                    Log.d(TAG, "시작함 ")
                }
                // 중지 되었을 때
                ACTION_PAUSE_SERVICE ->{
                    Log.d(TAG, "중지 ")
                }
                // 종료 되었을 때
                ACTION_STOP_SERVICE ->{
                    Log.d(TAG, "종료 ")
                }
                else -> null
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}