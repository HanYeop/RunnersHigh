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

    // 날짜 순으로 정렬
    @Query("SELECT * FROM run_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>
}