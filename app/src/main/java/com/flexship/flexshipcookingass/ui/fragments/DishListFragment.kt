package com.flexship.flexshipcookingass.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.adapters.DishAdapter
import com.flexship.flexshipcookingass.databinding.FragmentDishListBinding
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.other.Constants
import com.flexship.flexshipcookingass.other.DISH_ID_SAFE_ARG
import com.flexship.flexshipcookingass.other.getTitleCategory
import com.flexship.flexshipcookingass.services.CookService
import com.flexship.flexshipcookingass.ui.dialogs.DialogFragmentToDelete
import com.flexship.flexshipcookingass.ui.other.MainActivity
import com.flexship.flexshipcookingass.ui.viewmodels.DishListViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DishListFragment : Fragment(), DishAdapter.OnDishClick {

    private lateinit var binding: FragmentDishListBinding
    private lateinit var dishAdapter: DishAdapter
    private val viewModel: DishListViewModel by viewModels()
    private val args: DishListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dish_list, container, false)

        binding = FragmentDishListBinding.bind(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            val dialogToDelete =
                parentFragmentManager.findFragmentByTag(Constants.TAG_DIALOG_DELETE) as DialogFragmentToDelete?

            dialogToDelete?.apply {
                setAction {
                    deleteDish()
                }
            }
        }

        (requireActivity() as MainActivity).supportActionBar?.apply {
            title = getTitleCategory(args.categoryId)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.recViewDishes.apply {
            layoutManager = LinearLayoutManager(context)
            dishAdapter = DishAdapter(context, this@DishListFragment)
            adapter = dishAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && !binding.dishFbAdd.isVisible) {
                        binding.dishFbAdd.isVisible = true
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if ((dy > 0 || dy < 0) && binding.dishFbAdd.isVisible) {
                        binding.dishFbAdd.isVisible = false
                    }
                }
            })
        }
        viewModel.getDishesByCategory(args.categoryId).observe(viewLifecycleOwner) { dishes ->
            dishAdapter.differ.submitList(dishes)
        }
    }

    override fun onStart() {
        super.onStart()

        binding.dishFbAdd.setOnClickListener {
            findNavController().navigate(R.id.action_dish_list_fragment_to_dish_fragment)
        }
    }

    override fun onDishStarted(dish: Dish) {

        if (!CookService.isWorking) {
            val bundle = Bundle().apply {
                putInt(DISH_ID_SAFE_ARG, dish.id)
            }
            findNavController().navigate(R.id.action_recipeListFragment_to_cookingFragment, bundle)
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert_title_is_cooking))
                .setMessage(getString(R.string.alert_message_is_cooking))
                .setPositiveButton(R.string.yes) { _, _ ->
                    val bundle = Bundle().apply {
                        putInt("dishId", CookService.currentDishId)
                        putInt("posInList", CookService.posInList)
                    }
                    findNavController().navigate(
                        R.id.action_recipeListFragment_to_cookingFragment,
                        bundle
                    )
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDishDeleted(dish: Dish) {
        viewModel.dishToDelete = dish
        callDialogToDelete(dish)
    }

    override fun onDishEdited(dish: Dish) {
        val bundle = Bundle().apply {
            putInt(DISH_ID_SAFE_ARG, dish.id)
        }
        findNavController().navigate(R.id.action_dish_list_fragment_to_dish_fragment, bundle)
    }

    private fun callDialogToDelete(dish: Dish) {
        DialogFragmentToDelete.newInstance(
            getString(R.string.dialog_del_m),
            getString(R.string.dialog_del_dish_t)
        ).apply {
            setAction {
                deleteDish()
            }
        }.show(parentFragmentManager, Constants.TAG_DIALOG_DELETE)
    }

    private fun deleteDish() {
        viewModel.deleteDish(viewModel.dishToDelete)
        Snackbar.make(
            requireView(),
            getString(R.string.dish_has_been_deleted),
            Snackbar.LENGTH_LONG
        )
            .setAction(getString(R.string.undo)) {
                viewModel.insertDish(viewModel.dishToDelete)
            }
            .show()
    }


}