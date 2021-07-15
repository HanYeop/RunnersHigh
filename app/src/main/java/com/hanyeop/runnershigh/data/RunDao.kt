package com.hanyeop.runnershigh.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hanyeop.runnershigh.model.Run

@Dao
interface RunDao {

    // 기록 추가
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    // 기록 삭제
    @Delete
    suspend fun deleteRun(run: Run)

    // 모든 기록 삭제
    @Query("DELETE FROM run_table ")
    fun deleteAll()

    // 날짜 순으로 정렬
    @Query("SELECT * FROM run_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    // 시간 순으로 정렬
    @Query("SELECT * FROM run_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    // 거리 순으로 정렬
    @Query("SELECT * FROM run_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    // 평균 속도 순으로 정렬
    @Query("SELECT * FROM run_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    // 칼로리 순으로 정렬
    @Query("SELECT * FROM run_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    // 시간 합계
    @Query("SELECT SUM(timeInMillis) FROM run_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    // 거리 합계
    @Query("SELECT SUM(distanceInMeters) FROM run_table")
    fun getTotalDistance(): LiveData<Float>

    // 평균 속도
    @Query("SELECT AVG(avgSpeedInKMH) FROM run_table")
    fun getTotalAvgSpeed(): LiveData<Float>

    // 칼로리 합계
    @Query("SELECT SUM(caloriesBurned) FROM run_table")
    fun getTotalCaloriesBurned(): LiveData<Long>

    // 최장 시간
    @Query("SELECT MAX(timeInMillis) FROM run_table")
    fun getMaxTimeInMillis(): LiveData<Long>

    // 최장 거리
    @Query("SELECT MAX(distanceInMeters) FROM run_table")
    fun getMaxDistance(): LiveData<Float>

}