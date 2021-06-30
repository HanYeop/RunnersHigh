package com.hanyeop.runnershigh.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.hanyeop.runnershigh.data.RunDatabase
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_FIRST_TIME_TOGGLE
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_NAME
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_WEIGHT
import com.hanyeop.runnershigh.util.Constants.Companion.SHARED_PREFERENCES_NAME
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

    // SharedPreferences 추가
    @Singleton
    @Provides
    fun provideSharedPreferences(app: Application) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    // 이름 추가
    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_NAME, "") ?: ""

    // 몸무게 추가
    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_WEIGHT, 80f)

    // 앱 처음 실행 여부 추가
    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) = sharedPreferences.getBoolean(
        KEY_FIRST_TIME_TOGGLE, true
    )
}