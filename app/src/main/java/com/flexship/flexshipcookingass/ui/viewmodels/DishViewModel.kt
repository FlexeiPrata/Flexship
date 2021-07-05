package com.flexship.flexshipcookingass.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.flexship.flexshipcookingass.CookRepository
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.models.Stages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DishViewModel @Inject constructor(
    val cookRepository: CookRepository
) :ViewModel() {

    fun insertDish(dish: Dish) = viewModelScope.launch(Dispatchers.IO){
        cookRepository.insertDish(dish)
    }

    fun insertStages(stages: List<Stages>) = viewModelScope.launch(Dispatchers.IO){
        cookRepository.insertStages(stages)
    }

    fun insertStage(stage: Stages) = viewModelScope.launch(Dispatchers.IO){
        cookRepository.insertStage(stage)
    }

    fun getDishById(dishId :Int) = cookRepository.getDishWithStages(dishId).asLiveData()
}