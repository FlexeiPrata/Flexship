package com.flexship.flexshipcookingass.ui.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.app.CoreComponentFactory
import androidx.core.content.ContextCompat
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
import com.flexship.flexshipcookingass.services.CookService
import com.flexship.flexshipcookingass.ui.other.MainActivity
import com.flexship.flexshipcookingass.ui.viewmodels.DishViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

@AndroidEntryPoint
class CookingFragment : Fragment() {

    private lateinit var binding: FragmentCookingBinding
    private val args: CookingFragmentArgs by navArgs()

    private lateinit var dish: Dish
    private var stageList = mutableListOf<Stages>()

    private val viewModel: DishViewModel by viewModels()


    private var currentStage: Stages? = null
    private var currentPos: Int = -1

    private var isCooking = false
    private var time = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPos = args.posInList + 1
        Log.d(LOG_ID, currentPos.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.getDishById(args.dishId).observe(viewLifecycleOwner) { dishWithStages ->
            subscribeToObservers()
            dishWithStages?.let {
                stageList = it.stages.toMutableList()
                dish = it.dish
                applyUI()
            }

            (requireActivity() as MainActivity).supportActionBar?.apply {
                title = "Блюдо : ${dish.name}"
            }
        }
        val view = inflater.inflate(R.layout.fragment_cooking, container, false)

        binding = FragmentCookingBinding.bind(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun subscribeToObservers() {
        CookService.timer.observe(viewLifecycleOwner) { time ->
            this.time = time
            binding.textViewFinisher.isVisible = time <= 0L


            binding.apply {
                textViewTimer.text = getFormattedTime(time)
                currentStage?.let {
                    val maxi = (it.time * 1000).toInt()
                    val progress = (this@CookingFragment.time).toInt()
                    progressBar.max = maxi
                    progressBar.progress = progress
                    textViewTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_green))
                }

            }
            CookService.isCooking.observe(viewLifecycleOwner){
                updateToggle(it)
            }

        }
    }

    private fun getFormattedTime(time: Long): String {
        return String.format(
            getString(R.string.timer),
            zeroOrNotZero(time / 1000 / 60),
            zeroOrNotZero(time / 1000 % 60)
        )
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
                CookService.isWorking = false //только так пока работает
                CookService.isCooking.value = false
                followToNewStage()
            }

            fabPauseOrResume.setOnClickListener {
                subscribeToObservers()
                if (time <= 0L && CookService.isWorking) return@setOnClickListener
                if (isCooking) {
                    sendCommandToService(Constans.ACTION_PAUSE)
                    textViewTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_silver))
                } else {
                    sendCommandToService(Constans.ACTION_START_RESUME)

                }
            }
            fabStop.setOnClickListener {
                sendCommandToService(Constans.ACTION_STOP)
                findNavController().popBackStack()
            }

            fabStages.setOnClickListener {
                val bundle = Bundle().apply {
                    putInt(DISH_ID_SAFE_ARG, dish.id)
                    putInt(POS_IN_LIST_SAFE_ARG, currentPos - 1)
                }
                findNavController().navigate(R.id.action_cookingFragment_to_recipeStagesCookingFragment2, bundle)
            }
        }
    }

    private fun followToNewStage(posInList: Int = -1) {
        currentPos++
        if ((currentPos + 1) == stageList.size) {
            binding.fabNext.apply {
                isClickable = false
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black_mild))
            }
        }
        applyUI()
    }

    private fun applyUI() {
        currentStage = stageList[currentPos]
        binding.apply {
            val assertedTime = CookService.timer.value ?: 1
            if (assertedTime <= 0L && CookService.isWorking) {
                binding.textViewTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_green))
                binding.textViewFinisher.isVisible = true
                binding.textViewTimer.text = getString(R.string.zeroTimer)
            } else {
                textViewFinisher.isVisible = false
                textViewTimer.text = getFormattedTime(currentStage!!.time * 1000L)
                progressBar.progress = 0
                textViewTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_silver))
            }
            textViewName.text = currentStage?.name
        }


    }

    private fun sendCommandToService(actionToDo: String) {
        Intent(requireContext(), CookService::class.java).apply {
            action = actionToDo
            if (!CookService.isWorking) {
                putExtra(Constans.KEY_DISH_ID, args.dishId)
                putExtra(Constans.KEY_POSITION_IN_LIST, currentPos - 1)
                putExtra(Constans.KEY_TIME, currentStage!!.time * 1000L)
            }
        }.also {
            requireContext().startService(it)
            if (actionToDo == Constans.ACTION_STOP) CookService.timer.removeObservers(viewLifecycleOwner)
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