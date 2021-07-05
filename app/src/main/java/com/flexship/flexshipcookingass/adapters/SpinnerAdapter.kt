package com.flexship.flexshipcookingass.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.SpinnerItemBinding

class SpinnerAdapter(
    context: Context,
    private val text: Array<String>,
    private val images: Array<Int>
) : ArrayAdapter<String>(context, R.layout.spinner_item,text) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, parent)
    }

    private fun getCustomView(position: Int,parent: ViewGroup): View{
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_item,parent,false)

        val binding = SpinnerItemBinding.bind(view)

        binding.spinnerImage.setImageResource(images[position])
        binding.spinnerText.text = text[position]

        return view
    }
}