package com.flexship.flexshipcookingass.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.adapters.CategoryAdapter
import com.flexship.flexshipcookingass.databinding.FragmentCategoryBinding
import com.flexship.flexshipcookingass.models.Category
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.other.getTitleCategory
import com.flexship.flexshipcookingass.ui.viewmodels.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryFragment : Fragment(), CategoryAdapter.OnCategoryClick {

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories: MutableList<Category> = mutableListOf()

    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        binding = FragmentCategoryBinding.bind(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = "Категории"
        }

        binding.recViewCategory.apply {
            layoutManager = LinearLayoutManager(context)
            categoryAdapter = CategoryAdapter(context, this@CategoryFragment)
            adapter = categoryAdapter
        }

        viewModel.dishesToObserve.observe(viewLifecycleOwner) { dishes ->
            getCategories(dishes)
        }

    }

    override fun onStart() {
        super.onStart()
        binding.catFbAdd.setOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_dishFragment)
        }
    }

    private fun getCategories(dishes: List<Dish>) {
        var tr = false
        for (dish in dishes) {
            for (category in categories) {
                if (dish.category == category.id)
                    tr = true
            }
            if (!tr) {
                val name = getTitleCategory(dish.category)
                val image = when (dish.category) {
                    0 -> R.drawable.soup
                    1 -> R.drawable.snack
                    2 -> R.drawable.salad
                    3 -> R.drawable.pizza
                    4 -> R.drawable.thanksgiving
                    5 -> R.drawable.breakfast
                    6 -> R.drawable.vegan
                    else -> R.drawable.empty
                }
                val cat = Category(name, image, dish.category)
                categories.add(cat)
            }
            tr = false
        }
        categoryAdapter.differ.submitList(categories.toList())
    }

    override fun onCategoryClicked(categoryId: Int) {
        val bundle = Bundle().apply {
            putInt("categoryId", categoryId)
        }
        findNavController().navigate(R.id.action_categoryFragment_to_recipeListFragment, bundle)
    }

}