package com.flexship.flexshipcookingass.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stages_table")
data class Stages(
    @PrimaryKey (autoGenerate = true) val id: Int,
    val name: String,
    val time: Long
)
