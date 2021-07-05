package com.flexship.flexshipcookingass.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.FragmentCategoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_category, container, false)

        binding= FragmentCategoryBinding.bind(view)

        return view
    }

    override fun onStart() {
        super.onStart()
        binding.catFbAdd.setOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_dishFragment)
        }
    }

}