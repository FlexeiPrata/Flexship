package com.flexship.flexshipcookingass.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import com.flexship.flexshipcookingass.other.Constans
import com.flexship.flexshipcookingass.other.zeroOrNotZero
import com.flexship.flexshipcookingass.services.CookService
import com.flexship.flexshipcookingass.ui.viewmodels.DishViewModel
import dagger.hilt.android.AndroidEntryPoint

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

    private var isCooking= false
    private var isNewStage=true

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

            stageList = dishWithStages.stages
            dish = dishWithStages.dish

            stageAdapter.differ.submitList(stageList)

            followToNewStage()
        }
        subscribeToObservers()

    }

    private fun subscribeToObservers(){
        CookService.timer.observe(viewLifecycleOwner){
            time->
            if(time==0L){
                binding.textView2.isVisible=true
            }
            binding.textViewTimer.text="${zeroOrNotZero(time / 1000 / 60)}:${zeroOrNotZero(time / 1000 % 60)}"

        }
        CookService.isCooking.observe(viewLifecycleOwner){
            updateToggle(it)
        }
    }

    private fun updateToggle(isCooking: Boolean) {

        this.isCooking=isCooking
        if(isCooking){
            binding.fabPauseOrResume.setImageResource(R.drawable.ic_baseline_pause_24)
        }else{
            binding.fabPauseOrResume.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }

    override fun onStart() {
        super.onStart()

        binding.fabNext.setOnClickListener {
            isNewStage=true
            sendCommandToService(Constans.ACTION_STOP)
            followToNewStage()
        }
        binding.fabPauseOrResume.setOnClickListener {
            if(isCooking){
                sendCommandToService(Constans.ACTION_PAUSE)
            }else{
                sendCommandToService(Constans.ACTION_START_RESUME)
            }
        }
        binding.fabStop.setOnClickListener {
            sendCommandToService(Constans.ACTION_STOP)
        }
    }

    private fun followToNewStage() {
        currentStage=stageAdapter.differ.currentList[currentPos++]
        binding.textViewName.text="Текущий этап-".plus(currentStage?.name)
    }

    private fun sendCommandToService(actionToDo:String){
        Intent(requireContext(),CookService::class.java).apply {
            action=actionToDo
            if(isNewStage){
                putExtra(Constans.KEY_TIME,currentStage!!.time*1000L)
                isNewStage=false
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