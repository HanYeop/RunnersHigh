package com.hanyeop.runnershigh.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.hanyeop.runnershigh.model.Run

@Dao
interface RunDao {

    // 기록 추가
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    // 기록 삭제
    @Delete
    suspend fun deleteRun(run: Run)
}