package com.flexship.flexshipcookingass.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.databinding.StageAdapterBinding
import com.flexship.flexshipcookingass.helpers.DragAndDropSwappable
import com.flexship.flexshipcookingass.models.Stages
import com.flexship.flexshipcookingass.other.zeroOrNotZero

class StageAdapter(
    private val context: Context
) : RecyclerView.Adapter<StageAdapter.ViewHolderData>() {


    var items = mutableListOf<Stages>()
    var currentStage = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderData {
        val view = LayoutInflater.from(context).inflate(R.layout.stage_adapter, parent, false)
        return ViewHolderData(view)
    }

    override fun onBindViewHolder(holder: ViewHolderData, position: Int) {
        holder.setData(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolderData(itemView: View) : RecyclerView.ViewHolder(itemView),
        DragAndDropSwappable.ItemDragDropMoveViewHolder {
        private val binding: StageAdapterBinding = StageAdapterBinding.bind(itemView)

        fun setData(stages: Stages) = with(binding) {
            if (stages.time > 0) {
                stageTime.visibility = View.VISIBLE
                imageView4.visibility = View.VISIBLE
                stageTime.text = String.format(
                    context.getString(R.string.timer), zeroOrNotZero(stages.time / 60),
                    zeroOrNotZero(stages.time % 60)
                )
            } else {
                stageTime.visibility = View.GONE
                imageView4.visibility = View.GONE
            }
            stageName.text = stages.name
            if (adapterPosition == currentStage)
                imageView3.setImageResource(R.drawable.ra_stage_fill)
            else imageView3.setImageResource(R.drawable.ra_stage)
        }


        override fun onItemClear() {

        }

        override fun onItemDelete() {

        }

        override fun onItemEdit() {
            notifyItemChanged(adapterPosition)
        }

    }

    inner class StageItemDiffCallback(
        var oldList: List<Stages>,
        var newList: List<Stages>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

    }

    fun submitList(stageList: List<Stages>, currentStage: Int = -1) {
        val oldList = ArrayList(items)
        this.currentStage = currentStage

        if (currentStage != -1) notifyDataSetChanged()

        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            StageItemDiffCallback(oldList, stageList)
        )
        items = stageList.toMutableList()
        diffResult.dispatchUpdatesTo(this)


    }


}