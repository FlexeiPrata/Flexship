package com.flexship.flexshipcookingass.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "stages_table")
data class Stages(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val name: String = "",
    val time: Long = 0L,
    val dishId: Int = 0,
    var isCooking:Boolean=false
)
