package com.flexship.flexshipcookingass.models

import android.os.Parcelable
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.flexship.flexshipcookingass.other.LOG_ID
import java.io.Serializable


@Entity(tableName = "stages_table")
data class Stages(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String = "",
    var time: Long = 0L,
    val dishId: Int = 0,
    var isCooking: Boolean = false
)
