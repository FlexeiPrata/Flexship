package com.flexship.flexshipcookingass.db

import androidx.room.*
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.models.DishWithStages
import com.flexship.flexshipcookingass.models.Stages
import kotlinx.coroutines.flow.Flow

@Dao
interface CookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: Dish)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(stages: List<Stages>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStage(stages: Stages)

    @Query("SELECT MAX(id) from dish_table")
    fun getNewDish(): Flow<Int?>

    @Delete
    suspend fun deleteDish(dish: Dish)

    @Query("SELECT * FROM dish_table")
    fun getDishes(): Flow<List<Dish>>

    @Query("DELETE FROM stages_table where dishId = :dishId")
    suspend fun deleteStages(dishId: Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDish(dish: Dish)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStages(stages: List<Stages>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStage(stage: Stages)

    @Transaction
    @Query("SELECT * FROM dish_table where id = :dishId")
    fun getDishWithStages(dishId: Int): Flow<DishWithStages>

    @Query("SELECT * FROM dish_table where category = :category")
    fun getDishesByCategory(category: Int): Flow<List<Dish>>

    @Query("SELECT * FROM dish_table where category = :category ORDER BY name ASC")
    fun getDishesByCategorySortedByName(category: Int): Flow<List<Dish>>

    @Query("Select category From dish_table")
    fun getAllCategories(): Flow<List<Int>>

    @Delete
    suspend fun deleteStage(stage: Stages)

    @Query("DELETE FROM stages_table where id in (:ids)")
    suspend fun deleteNotSavedStages(ids: List<Int>)

    @Query("SELECT MAX(id) from stages_table")
    fun getMaxIdOfStage(): Flow<Int?>


}