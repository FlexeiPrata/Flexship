package com.flexship.flexshipcookingass.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.flexship.flexshipcookingass.ui.other.CookRepository
import com.flexship.flexshipcookingass.models.Dish
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DishListViewModel @Inject constructor(
    val cookRepository: CookRepository
) :ViewModel() {


    lateinit var dishToDelete:Dish

    fun getDishesByCategory(category: Int) = cookRepository.getDishesByCategory(category).asLiveData()

    fun deleteDish(dish: Dish) = viewModelScope.launch(Dispatchers.IO){
        cookRepository.deleteDish(dish)
        deleteStages(dish.id)
    }

    private fun deleteStages(dishId:Int) = viewModelScope.launch(Dispatchers.IO){
        cookRepository.deleteStages(dishId)
    }

    fun insertDish(dish: Dish) = viewModelScope.launch(Dispatchers.IO){
        cookRepository.insertDish(dish)
    }
}