package com.hanyeop.runnershigh.repository

import com.hanyeop.runnershigh.data.RunDao
import com.hanyeop.runnershigh.model.Run
import javax.inject.Inject

// 앱에서 사용하는 데이터와 그 데이터 통신을 하는 역할
class RunRepository @Inject constructor(
    private val runDao: RunDao
) {

    suspend fun insertRun(run : Run) = runDao.insertRun(run)
    suspend fun deleteRun(run : Run) = runDao.deleteRun(run)
    fun deleteAllRun() = runDao.deleteAll()

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()
    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()
    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()
    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()
    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalDistance() = runDao.getTotalDistance()
    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()
    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()
    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()
    fun getMaxTimeInMillis() = runDao.getMaxTimeInMillis()
    fun getMaxDistanceInMillis() = runDao.getMaxDistance()

}