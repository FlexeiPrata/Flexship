package com.flexship.flexshipcookingass.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.adapters.DishAdapter
import com.flexship.flexshipcookingass.databinding.FragmentDishListBinding
import com.flexship.flexshipcookingass.models.Dish
import com.flexship.flexshipcookingass.other.Constans
import com.flexship.flexshipcookingass.ui.dialogs.DialogFragmentToDelete
import com.flexship.flexshipcookingass.ui.viewmodels.DishListViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DishListFragment : Fragment(),DishAdapter.onDishClick {

    private lateinit var binding:FragmentDishListBinding

    private lateinit var dishAdapter: DishAdapter

    private val viewModel:DishListViewModel by viewModels()

    private val args:DishListFragmentArgs by navArgs()

    private var dishToDelete:Dish?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_dish_list, container, false)

        binding= FragmentDishListBinding.bind(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(savedInstanceState!=null){
            val dialogToDelete=parentFragmentManager.findFragmentByTag(Constans.TAG_DIALOG_DELETE) as DialogFragmentToDelete?

            dialogToDelete?.apply {
                setAction {
                    viewModel.deleteDish(dishToDelete!!)
                    Snackbar.make(requireView(),"Dish was successfully deleted!",Snackbar.LENGTH_LONG)
                        .setAction("UNDO"){
                            viewModel.insertDish(dishToDelete!!)
                        }
                        .show()
                }
            }
        }

        binding.recViewDishes.apply {
            layoutManager=LinearLayoutManager(context)
            dishAdapter= DishAdapter(context,this@DishListFragment)
            adapter=dishAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if(newState==RecyclerView.SCROLL_STATE_IDLE && !binding.dishFbAdd.isVisible){
                        binding.dishFbAdd.isVisible=true
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if((dy>0 || dy<0) && binding.dishFbAdd.isVisible){
                        binding.dishFbAdd.isVisible=false
                    }
                }
            })
        }

        viewModel.getDishesByCategory(args.categoryId).observe(viewLifecycleOwner){
            dishes->
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
        val bundle=Bundle().apply {
            putInt("dishId",dish.id)
        }
        findNavController().navigate(R.id.action_dish_list_fragment_to_dish_fragment,bundle)
    }

    override fun onDishDeleted(dish: Dish) {
        dishToDelete=dish
        callDialogToDelete(dish)
    }

    override fun onDishEdited(dish: Dish) {
        TODO("Not yet implemented")
    }

    private fun callDialogToDelete(dish: Dish){
        DialogFragmentToDelete().apply {
            setAction {
                viewModel.deleteDish(dish)
                Snackbar.make(requireView(),"Dish was successfully deleted!",Snackbar.LENGTH_LONG)
                    .setAction("UNDO"){
                        viewModel.insertDish(dish)
                    }
                    .show()
            }
        }.show(parentFragmentManager,Constans.TAG_DIALOG_DELETE)
    }


}