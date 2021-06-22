package com.hanyeop.runnershigh.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hanyeop.runnershigh.model.Run

/* entities = 사용할 엔티티 선언, version = 엔티티 구조 변경 시 구분해주는 역할
   exportSchema = 스키마 내보내기 설정 */
@Database(entities = [Run::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RunDatabase : RoomDatabase() {

    abstract fun runDao() : RunDao
}