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
) : RecyclerView.Adapter<StageAdapter.ViewHolderData>() {

    var items = mutableListOf<Stages>()

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
            }
            else {
                stageTime.visibility = View.GONE
                imageView4.visibility = View.GONE
            }
            stageName.text = stages.name
            if (stages.isCooking)
                imageView3.setImageResource(R.drawable.ra_stage_fill)
        }

        override fun onItemSelected() {

        }

        override fun onItemClear() {

        }

        override fun onItemDelete() {

        }

        override fun onItemEdit() {
            //notifyItemChanged(adapterPosition)
        }

    }


   /* private val diffUtil = object : DiffUtil.ItemCallback<Stages>() {
        override fun areItemsTheSame(oldItem: Stages, newItem: Stages): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Stages, newItem: Stages): Boolean = oldItem == newItem
    }
    val differ = AsyncListDiffer(this, diffUtil)*/

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

    fun submitList(stageList: List<Stages>) {
        val oldList = ArrayList(items)
        /*Log.d(LOG_ID, "OLD LIST: ")
        for (i in oldList) Log.d(LOG_ID, "$i")
        Log.d(LOG_ID, "NEW LIST: ")
        for (i in stageList) Log.d(LOG_ID, "$i")*/
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            StageItemDiffCallback(oldList, stageList)
        )
        items = stageList.toMutableList()
        diffResult.dispatchUpdatesTo(this)



    }


}