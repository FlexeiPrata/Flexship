package com.flexship.flexshipcookingass.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.FragmentDishBinding
import com.flexship.flexshipcookingass.other.DISH_ID_SAFE_ARG

class DishFragment : Fragment() {
    private var DishId = 0

    private lateinit var _binding: FragmentDishBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
            DishId = it.getInt(DISH_ID_SAFE_ARG)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishBinding.inflate(inflater, container, false)
        return binding.root
    }
}