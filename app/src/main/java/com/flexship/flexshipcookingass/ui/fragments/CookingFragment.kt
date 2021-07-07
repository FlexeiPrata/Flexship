package com.flexship.flexshipcookingass.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.adapters.StageAdapter
import com.flexship.flexshipcookingass.databinding.FragmentCookingBinding
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.models.Stages
import com.flexship.flexshipcookingass.other.*
import com.flexship.flexshipcookingass.other.expandAction
import com.flexship.flexshipcookingass.services.CookService
import com.flexship.flexshipcookingass.ui.viewmodels.DishViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

@AndroidEntryPoint
class CookingFragment : Fragment() {

    private lateinit var binding: FragmentCookingBinding
    private val args: CookingFragmentArgs by navArgs()

    private lateinit var dish: Dish
    private var stageList: List<Stages> = listOf()

    private val viewModel: DishViewModel by viewModels()

    private lateinit var stageAdapter: StageAdapter

    private var currentStage: Stages? = null
    private var currentPos: Int = 0

    private var isCooking = false
    private var isNewStage = true

    private var isExpanded = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cooking, container, false)

        binding = FragmentCookingBinding.bind(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Приготовление блюда"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.recViewStage.apply {
            layoutManager = LinearLayoutManager(context)
            stageAdapter = StageAdapter(context)
            adapter = stageAdapter
        }

        viewModel.getDishById(args.dishId).observe(viewLifecycleOwner) { dishWithStages ->

            dishWithStages?.let {
                stageList = it.stages
                dish = it.dish
            }
            stageAdapter.differ.submitList(stageList)

            followToNewStage()
        }
        subscribeToObservers()


    }

    private fun subscribeToObservers() {
        Log.d("Zalupa", "OnObserve")
        CookService.timer.observe(viewLifecycleOwner) { time ->
            if (time == 0L) {
                binding.textView2.isVisible = true
            }
            binding.textViewTimer.text = String.format(
                getString(R.string.timer),
                zeroOrNotZero(time / 1000 / 60),
                zeroOrNotZero(time / 100 % 60)
            )

        }
        CookService.isCooking.observe(viewLifecycleOwner) {
            Log.d("Zalupa", "HUI + $it")
            updateToggle(it)
        }
    }

    private fun updateToggle(isCooking: Boolean) {

        this.isCooking = isCooking
        if (isCooking) {
            binding.fabPauseOrResume.setImageResource(R.drawable.ic_baseline_pause_24)
        } else {
            binding.fabPauseOrResume.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.apply {

            fabNext.setOnClickListener {
                sendCommandToService(Constans.ACTION_STOP)
                followToNewStage()
                isNewStage = true
            }

            fabPauseOrResume.setOnClickListener {
                if (isCooking) {
                    sendCommandToService(Constans.ACTION_PAUSE)
                } else {
                    sendCommandToService(Constans.ACTION_START_RESUME)
                }
            }
            fabStop.setOnClickListener {
                sendCommandToService(Constans.ACTION_STOP)
                findNavController().popBackStack()
            }

            expandCollapse.setOnClickListener {
                if (isExpanded) {
                    collapse(rvBox)
                    isExpanded = false
                    it.animate().setDuration(200).rotation(180F)
                } else {

                    expand(rvBox)
                    isExpanded = true
                    it.animate().setDuration(200).rotation(0F)
                }
            }
        }
    }

    private fun followToNewStage() {
        try {
            currentStage = stageAdapter.differ.currentList[currentPos++]
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        binding.textViewName.text = "Текущий этап - ".plus(currentStage?.name)
        binding.textView2.isVisible = false
        binding.textViewTimer.text = ""
    }

    private fun sendCommandToService(actionToDo: String) {
        Intent(requireContext(), CookService::class.java).apply {
            action = actionToDo
            if (isNewStage) {
                Log.d("Zalupa", "Huinea rabotai ${currentStage!!.time * 1000L}")
                putExtra(Constans.KEY_TIME, currentStage!!.time * 1000L)
                isNewStage = false
            }
        }.also {
            requireContext().startService(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}