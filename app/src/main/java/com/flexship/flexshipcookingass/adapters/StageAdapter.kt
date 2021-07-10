package com.flexship.flexshipcookingass.adapters

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.StageAdapterBinding
import com.flexship.flexshipcookingass.helpers.DragAndDropSwappable
import com.flexship.flexshipcookingass.models.Stages
import com.flexship.flexshipcookingass.other.LOG_ID
import com.flexship.flexshipcookingass.other.zeroOrNotZero

class StageAdapter(
    private val context: Context
) :RecyclerView.Adapter<StageAdapter.ViewHolderData>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderData {
        val view = LayoutInflater.from(context).inflate(R.layout.stage_adapter, parent,false)
        return ViewHolderData(view)
    }

    override fun onBindViewHolder(holder: ViewHolderData, position: Int) {
        holder.setData(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class ViewHolderData(itemView: View) :RecyclerView.ViewHolder(itemView), DragAndDropSwappable.ItemDragDropMoveViewHolder {
        private val binding: StageAdapterBinding = StageAdapterBinding.bind(itemView)

        fun setData(stages: Stages) = with(binding) {
            if (stages.time > 0) stageTime.text = String.format(context.getString(R.string.timer), zeroOrNotZero(stages.time / 60),
                zeroOrNotZero( stages.time % 60))
            else {
                stageTime.visibility = View.GONE
                imageView4.visibility = View.GONE
            }
            stageName.text = stages.name
            if(stages.isCooking)
                imageView3.setImageResource(R.drawable.ra_stage_fill)
        }

        override fun onItemSelected() {

        }

        override fun onItemClear() {

        }

        override fun onItemDelete() {

        }

        override fun onItemEdit() {
            notifyItemChanged(adapterPosition)
        }

    }

    private val diffUtil = object: DiffUtil.ItemCallback<Stages>() {
        override fun areItemsTheSame(oldItem: Stages, newItem: Stages): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Stages, newItem: Stages): Boolean {
            return  oldItem.hashCode() == newItem.hashCode()
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)


}