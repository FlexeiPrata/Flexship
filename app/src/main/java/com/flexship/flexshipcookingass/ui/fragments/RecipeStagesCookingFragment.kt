package com.flexship.flexshipcookingass.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.flexship.flexshipcookingass.adapters.StageAdapter
import com.flexship.flexshipcookingass.databinding.FragmentRecipeStagesCookingBinding
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.models.Stages
import com.flexship.flexshipcookingass.ui.viewmodels.DishViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeStagesCookingFragment : Fragment() {

    private lateinit var _binding: FragmentRecipeStagesCookingBinding
    private val binding get() = _binding

    private val viewModel: DishViewModel by viewModels()
    private val args: CookingFragmentArgs by navArgs()

    private var stageList = listOf<Stages>()
    private lateinit var dish: Dish


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeStagesCookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getDishById(args.dishId).observe(viewLifecycleOwner) { dishWithStages ->
            dishWithStages?.let {
                stageList = it.stages.toMutableList()
                dish = it.dish
                updateUI()
            }
        }

    }

    private fun updateUI() {
        binding.apply {
            recycler.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = StageAdapter(context).also {
                    it.submitList(stageList, args.posInList + 1)
                }
            }
            textViewRecipe.text = dish.recipe
        }
    }
}