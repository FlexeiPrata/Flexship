package com.flexship.flexshipcookingass.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.CategoryItemBinding
import com.flexship.flexshipcookingass.models.Category

class CategoryAdapter(
    private val context: Context,
    private val onCategoryClick: OnCategoryClick
) : RecyclerView.Adapter<CategoryAdapter.ViewHolderData>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderData {
        val view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false)
        return ViewHolderData(view)
    }

    override fun onBindViewHolder(holder: ViewHolderData, position: Int) {
        holder.setData(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class ViewHolderData(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        private val binding = CategoryItemBinding.bind(itemView)
        init {
            itemView.setOnClickListener(this)
        }

        fun setData(category: Category) = with(binding) {
            categoryName.text = category.name
            categoryImage.setImageResource(category.image)
        }

        override fun onClick(v: View?) {
            onCategoryClick.onCategoryClicked(differ.currentList[adapterPosition].id)
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)

    interface OnCategoryClick{
        fun onCategoryClicked(categoryId:Int)
    }
}