package com.flexship.flexshipcookingass

import com.flexship.flexshipcookingass.db.CookDao
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.models.Stages
import javax.inject.Inject

class CookRepository @Inject constructor(
    val cookDao : CookDao
) {

    suspend fun insertDish(dish: Dish) = cookDao.insertDish(dish)

    suspend fun insertStages(stages:List<Stages> ) = cookDao.insertStages(stages)

    suspend fun insertStage(stages: Stages) = cookDao.insertStage(stages)

    suspend fun deleteDish(dish: Dish) = cookDao.deleteDish(dish)

    suspend fun deleteStages(dishId:Int) = cookDao.deleteStages(dishId)

    suspend fun updateDish(dish: Dish) = cookDao.updateDish(dish)

    suspend fun updateStages(stages: List<Stages>) = cookDao.updateStages(stages)

    fun getDishesByCategory(category:Int) = cookDao.getDishesByCategory(category)

    fun getDishWithStages(dishId: Int) = cookDao.getDishWithStages(dishId)

    fun getDishesByCategorySortedByName(category: Int) = cookDao.getDishesByCategorySortedByName(category)

}