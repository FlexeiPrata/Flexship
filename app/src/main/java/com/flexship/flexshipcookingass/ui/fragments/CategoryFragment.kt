package com.flexship.flexshipcookingass.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.adapters.CategoryAdapter
import com.flexship.flexshipcookingass.databinding.FragmentCategoryBinding
import com.flexship.flexshipcookingass.models.Category
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.other.CATEGORY_ID
import com.flexship.flexshipcookingass.other.getTitleCategory
import com.flexship.flexshipcookingass.services.CookService
import com.flexship.flexshipcookingass.ui.other.MainActivity
import com.flexship.flexshipcookingass.ui.viewmodels.CategoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        val view = inflater.inflate(R.layout.fragment_category, container, false)
        binding = FragmentCategoryBinding.bind(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.categoriesID.observe(viewLifecycleOwner) {
            it?.let {
                updateUI(it)
            }
        }

        (requireActivity() as MainActivity).supportActionBar?.apply {
            title="Категории"
            setDisplayHomeAsUpEnabled(false)
        }

    }

    override fun onStart() {
        super.onStart()
        binding.catFbAdd.setOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_dishFragment)
        }
    }


    override fun onCategoryClicked(categoryId: Int) {
        val bundle = Bundle().apply {
            putInt(CATEGORY_ID, categoryId)
        }
        findNavController().navigate(R.id.action_categoryFragment_to_recipeListFragment, bundle)
    }

    private fun updateUI(list: List<Int>) {
        categories.clear()
        val catListInts = mutableListOf<Int>()
        for (i in list) {
            if (!catListInts.contains(i)) catListInts.add(i)
        }

        for (i in catListInts) {
            val name = getTitleCategory(i)
            val image = when (i) {
                0 -> R.drawable.soup
                1 -> R.drawable.snack
                2 -> R.drawable.salad
                3 -> R.drawable.pizza
                4 -> R.drawable.thanksgiving
                5 -> R.drawable.breakfast
                6 -> R.drawable.vegan
                else -> R.drawable.empty
            }
            val cat = Category(name, image, i)
            categories.add(cat)
        }

        binding.recViewCategory.apply {
            layoutManager = LinearLayoutManager(context)
            categoryAdapter = CategoryAdapter(context, this@CategoryFragment)
            adapter = categoryAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
        categoryAdapter.differ.submitList(categories.toList())

    }

}