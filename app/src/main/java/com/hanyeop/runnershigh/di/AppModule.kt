package com.hanyeop.runnershigh.di

import android.app.Application
import androidx.room.Room
import com.hanyeop.runnershigh.data.RunDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 데이터 베이스 추가
    @Singleton
    @Provides
    fun provideRunDatabase(app : Application) =
        Room.databaseBuilder(app,RunDatabase::class.java,"run_db")
            .fallbackToDestructiveMigration() // 버전 변경 시 기존 데이터 삭제
            .build()

    // Dao 추가
    @Singleton
    @Provides
    fun provideRunDao(db : RunDatabase) = db.runDao()


}