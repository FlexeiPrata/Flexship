package com.flexship.flexshipcookingass.adapters

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.StageAdapterBinding
import com.flexship.flexshipcookingass.models.Stages

class StageAdapter(
    private val context: Context
) :RecyclerView.Adapter<StageAdapter.ViewHolderData>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderData {
        val view=LayoutInflater.from(context).inflate(R.layout.stage_adapter,parent,false)
        return ViewHolderData(view)
    }

    override fun onBindViewHolder(holder: ViewHolderData, position: Int) {
        holder.setData(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    class ViewHolderData(itemView: View) :RecyclerView.ViewHolder(itemView) {
        private val binding:StageAdapterBinding = StageAdapterBinding.bind(itemView)

        fun setData(stages: Stages)= with(binding){
            stageTime.text = stages.time.toString().plus(" min")
            stageName.text = stages.name
        }
    }

    private val diffUtil=object: DiffUtil.ItemCallback<Stages>() {
        override fun areItemsTheSame(oldItem: Stages, newItem: Stages): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Stages, newItem: Stages): Boolean {
            return  oldItem.hashCode()==newItem.hashCode()
        }
    }
    val differ=AsyncListDiffer(this,diffUtil)

}