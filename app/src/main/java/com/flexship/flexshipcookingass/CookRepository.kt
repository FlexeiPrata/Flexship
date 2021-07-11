package com.flexship.flexshipcookingass

import androidx.lifecycle.asLiveData
import com.flexship.flexshipcookingass.db.CookDao
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.models.Stages
import javax.inject.Inject

class CookRepository @Inject constructor(
    val cookDao: CookDao
) {

    suspend fun insertDish(dish: Dish) = cookDao.insertDish(dish)

    suspend fun insertStages(stages: List<Stages>) = cookDao.insertStages(stages)

    suspend fun insertStage(stages: Stages) = cookDao.insertStage(stages)

    suspend fun deleteDish(dish: Dish) = cookDao.deleteDish(dish)

    suspend fun deleteStages(dishId: Int) = cookDao.deleteStages(dishId)

    suspend fun updateDish(dish: Dish) = cookDao.updateDish(dish)

    suspend fun updateStages(stages: List<Stages>) = cookDao.updateStages(stages)

    fun getDishesByCategory(category: Int) = cookDao.getDishesByCategory(category)

    fun getDishWithStages(dishId: Int) = cookDao.getDishWithStages(dishId)

    fun getDishesByCategorySortedByName(category: Int) =
        cookDao.getDishesByCategorySortedByName(category)

    fun getMaxIdDish() = cookDao.getNewDish()

    fun getDishes() = cookDao.getDishes()

    suspend fun deleteStage(stages: Stages) = cookDao.deleteStage(stages)

    fun getAllCategories() = cookDao.getAllCategories()

    suspend fun deleteNotSavedStages(ids: List<Int>) = cookDao.deleteNotSavedStages(ids)

    fun getMaxIdOfStage() = cookDao.getMaxIdOfStage()


}