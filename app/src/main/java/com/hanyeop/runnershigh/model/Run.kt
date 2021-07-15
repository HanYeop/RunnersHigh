package com.hanyeop.runnershigh.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_table")
data class Run(
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0,
    var image: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Float = 0f,
    var timeInMillis: Long = 0,
    var caloriesBurned: Int = 0,
    var title : String = "",
    var year : Int = 0,
    var month : Int = 0,
    var day : Int = 0
)