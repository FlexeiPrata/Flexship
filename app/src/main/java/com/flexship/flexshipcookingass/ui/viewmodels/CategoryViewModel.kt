package com.flexship.flexshipcookingass.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.flexship.flexshipcookingass.CookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    val cookRepository: CookRepository
): ViewModel() {

    val categoriesID = cookRepository.getAllCategories().asLiveData()
}