package com.flexship.flexshipcookingass.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.NumberPickerBinding
import kotlin.math.min

const val MINUTES="MINUTES"
class MinutePickerDialog: DialogFragment() {

    private var minutes: Int=0
    private var actionPickMinutes: ((Int)->Unit)?=null

    fun setAction(action:((Int)->Unit)){
        actionPickMinutes=action
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.number_picker,container,false)

        val binding= NumberPickerBinding.bind(view)


        binding.pickNumber.apply {
            maxValue=200
            minValue=0
            wrapSelectorWheel=false
            setOnValueChangedListener { picker, oldVal, newVal ->
                minutes=newVal
            }
        }
        if(savedInstanceState!=null){
            minutes=savedInstanceState.getInt(MINUTES)
            binding.pickNumber.value=minutes
        }

        binding.pickBSave.setOnClickListener {
            actionPickMinutes?.invoke(binding.pickNumber.value)
            dismiss()
        }
        binding.pickDismiss.setOnClickListener {
            dismiss()
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(MINUTES,minutes)
    }


}