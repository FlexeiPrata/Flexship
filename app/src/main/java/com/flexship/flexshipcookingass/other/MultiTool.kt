package com.flexship.flexshipcookingass.other

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import kotlin.math.roundToInt

const val DATABASE_NAME = "cook_database"
const val DISH_ID_SAFE_ARG = "dishId"
const val POS_IN_LIST_SAFE_ARG = "posInList"
const val MINUTES = "MINUTES"
const val CATEGORY_ID = "categoryId"
const val LOG_ID = "MyLog"
const val TITLE = "Title"
const val MESSAGE = "Message"

fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap =
        Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun convertDpToPx(context: Context, dp: Int): Int {
    return (dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun zeroOrNotZero(i: Number): String {
    if (i.toLong() > 9) return i.toString()
    else return "0$i"
}

fun getTitleCategory(id: Int): String {
    return when (id) {
        0 -> "Супы"
        1 -> "Закуски"
        2 -> "Салаты"
        3 -> "Пиццы"
        4 -> "Горячее"
        5 -> "Завтрак"
        6 -> "Десерты"
        else -> "CUM"
    }
}