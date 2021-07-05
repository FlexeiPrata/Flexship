package com.flexship.flexshipcookingass.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.NumberPickerBinding
import com.flexship.flexshipcookingass.other.MINUTES


class MinutePickerDialog: DialogFragment() {

    private var timeSec: Int = 0
    private var timeMin: Int = 0
    private var actionPickMinutes: ((Int)->Unit) ?= null
    private lateinit var binding: NumberPickerBinding

    fun setAction(action : ((Int)->Unit)) {
        actionPickMinutes = action
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.number_picker, container,false)

        binding = NumberPickerBinding.bind(view)


        binding.pickMinutes.apply {
            maxValue = 180
            minValue = 0
            wrapSelectorWheel = true
        }

        binding.pickSeconds.apply {
            maxValue = 59
            minValue = 0
            wrapSelectorWheel = true
        }

        timeSec = savedInstanceState?.getInt(MINUTES) ?: 0
        binding.pickSeconds.value = timeSec % 60
        binding.pickMinutes.value = timeMin / 60


        binding.pickBSave.setOnClickListener {
            timeSec = binding.pickSeconds.value + (binding.pickMinutes.value * 60)
            actionPickMinutes?.invoke(timeSec)
            dismiss()
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        timeSec = binding.pickSeconds.value + (binding.pickMinutes.value * 60)
        outState.putInt(MINUTES, timeSec)
    }


}