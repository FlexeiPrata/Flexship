package com.flexship.flexshipcookingass.helpers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.flexship.flexshipcookingass.R
import com.flexship.flexshipcookingass.other.convertDpToPx
import com.flexship.flexshipcookingass.other.drawableToBitmap

abstract class DragAndDropSwappable(private val context: Context) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
) {
    //Целевой ViewHolder должен реализовывать интерфейс DragAndDropSwappable.ItemDragDropMoveViewHolder

    private val iconDelete =
        drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ra_delete)!!)!!
    private val iconEdit =
        drawableToBitmap(AppCompatResources.getDrawable(context, R.drawable.ra_edit)!!)!!

    private val pDelete = Paint().apply {
        color = ContextCompat.getColor(context, R.color.red)
    }
    private val pEdit = Paint().apply {
        color = ContextCompat.getColor(context, R.color.purple_wildberry)
    }


    interface ItemDragDropMoveViewHolder {
        fun onItemClear() //При окончании передвижения
        fun onItemDelete() //При удалении, здесь необходимо реализовать сохранение в базе данных
        fun onItemEdit() //При редактировании, здесь необходимо реализовать сохранение в базе данных
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        if (viewHolder is ItemDragDropMoveViewHolder) {
            viewHolder.onItemClear()
        }
        super.clearView(recyclerView, viewHolder)
    }


    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )

        val itemView: View = viewHolder.itemView

        if (dX > 0) {
            c.drawRect(
                itemView.left.toFloat(), itemView.top.toFloat(), dX,
                itemView.bottom.toFloat(), pDelete
            )
            c.drawBitmap(
                iconDelete,
                itemView.left.toFloat() + convertDpToPx(context, 12),
                itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - iconDelete.height) / 2,
                pDelete
            )
        } else if (dX < 0) {
            c.drawRect(
                itemView.right.toFloat() + dX, itemView.top.toFloat(),
                itemView.right.toFloat(), itemView.bottom.toFloat(), pEdit
            )
            c.drawBitmap(
                iconEdit,
                itemView.right.toFloat() - convertDpToPx(context, 12) - iconEdit.width,
                itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - iconEdit.height) / 2,
                pEdit
            )
        }
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (direction == ItemTouchHelper.RIGHT) {
            if (viewHolder is ItemDragDropMoveViewHolder) viewHolder.onItemDelete()
            itemDelete(viewHolder.adapterPosition)
        }
        if (direction == ItemTouchHelper.LEFT) {
            if (viewHolder is ItemDragDropMoveViewHolder) viewHolder.onItemEdit()
            itemEdit(viewHolder.adapterPosition)
        }

    }

    abstract fun itemEdit(pos: Int)

    abstract fun itemDelete(pos: Int)


}