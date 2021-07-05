package com.flexship.flexshipcookingass.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class Converters {

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?):ByteArray?{
        return bitmap?.let {
            val out=ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG,30,out)
            out.toByteArray()
        }
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray?):Bitmap?{
        return byteArray?.let {
            BitmapFactory.decodeByteArray(it,0,it.size)
        }
    }
}