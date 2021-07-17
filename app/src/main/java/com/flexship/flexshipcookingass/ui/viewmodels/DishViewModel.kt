package com.flexship.flexshipcookingass.ui.viewmodels

import androidx.lifecycle.*
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.models.Stages
import com.flexship.flexshipcookingass.ui.other.CookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DishViewModel @Inject constructor(
    val cookRepository: CookRepository
) : ViewModel() {

    val mutableStageList = MutableLiveData<MutableList<Stages>>()

    val stageList get() = mutableStageList as LiveData<MutableList<Stages>>

    val bufferStageList = mutableListOf<Stages>()

    var stageToEdit: Stages? = null
    var posToEdit: Int = -1

    var isUpdated = false
    var isNewDish: Boolean = true
    var isChangedConfig: Boolean = false
    var isStageEdit = false
    var isBuffer = false

    fun postEmptyValues() {
        mutableStageList.postValue(mutableListOf())
    }

    fun insertDish(dish: Dish) = viewModelScope.launch(Dispatchers.IO) {
        cookRepository.insertDish(dish)
    }

    fun insertStages(stages: List<Stages>) = viewModelScope.launch(Dispatchers.IO) {
        cookRepository.insertStages(stages)
    }

    fun insertStage(stage: Stages) = viewModelScope.launch(Dispatchers.IO) {
        cookRepository.insertStage(stage)
    }

    fun updateStages(stages: List<Stages>) = viewModelScope.launch(Dispatchers.IO) {
        cookRepository.updateStages(stages)
    }

    fun deleteStage(stage: Stages) = viewModelScope.launch(Dispatchers.IO) {
        cookRepository.deleteStage(stage)
    }

    fun getDishById(dishId: Int) = cookRepository.getDishWithStages(dishId).asLiveData()

    fun getMaxIdDish() = cookRepository.getMaxIdDish().asLiveData()

    fun updateDish(dish: Dish, stages: List<Stages> = listOf(), updateStage: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            cookRepository.updateDish(dish)
            if (stages.isNotEmpty()) {
                if (updateStage) {
                    updateStages(stages)
                } else {
                    insertStages(stages)
                }
            }
        }

    fun deleteDish(dish: Dish, deleteStages: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            cookRepository.deleteDish(dish)
            if (deleteStages) {
                cookRepository.deleteStages(dish.id)
            }
        }

    fun deleteNotSavedStages(ids: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
        cookRepository.deleteNotSavedStages(ids)
    }

    fun updateStage(stage: Stages) = viewModelScope.launch(Dispatchers.IO) {
        cookRepository.updateStage(stage)
    }

    val getMaxIdStage = cookRepository.getMaxIdOfStage().asLiveData()


}