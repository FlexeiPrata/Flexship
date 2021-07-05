package com.flexship.flexshipcookingass.models

import androidx.room.Embedded
import androidx.room.Relation

data class DishWithStages(
    @Embedded
    val dish: Dish,
    @Relation(
        parentColumn = "id",
        entityColumn = "dishId"
    )
    val stages:List<Stages>
)
