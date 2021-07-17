package com.flexship.flexshipcookingass.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "stages_table")
data class Stages(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String = "",
    var time: Long = 0L,
    val dishId: Int = 0,
    var isCooking: Boolean = false
)
