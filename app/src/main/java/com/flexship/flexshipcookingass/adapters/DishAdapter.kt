package com.flexship.flexshipcookingass.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.DishAdapterBinding
import com.flexship.flexshipcookingass.models.Dish

class DishAdapter(
    private val context: Context,
    private val onDishClicked: onDishClick
) :RecyclerView.Adapter<DishAdapter.ViewHolderData>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderData {
        val view=LayoutInflater.from(context).inflate(R.layout.dish_adapter,parent,false)
        return ViewHolderData(view)
    }

    override fun onBindViewHolder(holder: ViewHolderData, position: Int) {
        holder.setData(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class ViewHolderData(itemView: View):RecyclerView.ViewHolder(itemView) {
        private val binding=DishAdapterBinding.bind(itemView)

        fun setData(dish:Dish)= with(binding){
            if(dish.image==null){
                dishImage.setImageResource(R.drawable.empty)
            }
            else{
                dishImage.setImageBitmap(dish.image)
            }

            dishName.text=dish.name
            dishCategory.text=when(dish.category){
                0-> "Супы"
                1-> "Закуски"
                2-> "Салаты"
                3-> "Пиццы"
                4-> "Горячее"
                5-> "Завтрак"
                6-> "Десерты"
                else-> "CUM"
            }

            dishBDelete.setOnClickListener {
                onDishClicked.onDishDeleted(dish)
            }
            dishBEdit.setOnClickListener {
                onDishClicked.onDishEdited(dish)
            }
            dishBStart.setOnClickListener {
                onDishClicked.onDishStarted(dish)
            }

        }

    }
    private val diffUtil=object :DiffUtil.ItemCallback<Dish>(){
        override fun areItemsTheSame(oldItem: Dish, newItem: Dish): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Dish, newItem: Dish): Boolean {
            return oldItem.hashCode()==newItem.hashCode()
        }

    }
    val differ=AsyncListDiffer(this,diffUtil)

    interface onDishClick{
        fun onDishStarted(dish: Dish)
        fun onDishDeleted(dish: Dish)
        fun onDishEdited(dish: Dish)
    }
}