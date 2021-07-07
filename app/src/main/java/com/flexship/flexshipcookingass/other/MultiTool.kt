package com.flexship.flexshipcookingass.other

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import kotlin.math.roundToInt

const val DATABASE_NAME = "cook_database"
const val DISH_ID_SAFE_ARG = "dishId"
const val MINUTES = "MINUTES"

//анимация плавного сжатия View
fun collapse(view: View): Animation {
    val actualHeight = view.measuredHeight
    val animation: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) view.visibility = View.GONE else {
                view.layoutParams.height = actualHeight - (actualHeight * interpolatedTime).toInt()
                view.requestLayout()
            }
        }
    }
    animation.duration = (actualHeight / view.context.resources.displayMetrics.density).toLong() * 2
    return animation
}

//анимация плавного расширения View
fun expandAction(view: View) {
    view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val actualHeight = view.measuredHeight
    view.layoutParams.height = 0
    view.visibility = View.VISIBLE
    val animation: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            view.layoutParams.height =
                if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT else (actualHeight * interpolatedTime).toInt()
            view.requestLayout()
        }
    }
    animation.duration = (actualHeight / view.context.resources.displayMetrics.density).toLong() * 2
    view.startAnimation(animation)
}

//функция для того чтобы спрятать Soft Keyboard из не активити
fun hideKeyboardFrom(context: Context, view: View) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

//конвертация Drawable в Bitmap, необходимая для рисования векторного элемента
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
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
    drawable.draw(canvas)
    return bitmap
}

//конвертация dp в пиксели
fun convertDpToPx(context: Context, dp: Int): Int {
    return (dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun zeroOrNotZero(i: Int): String {
    if (i > 9) return i.toString()
    else return "0$i"
}
fun zeroOrNotZero(i: Long): String {
    if (i > 9) return i.toString()
    else return "0$i"
}

fun zeroOrNotZero(i: Long): String {
    if (i > 9) return i.toString()
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