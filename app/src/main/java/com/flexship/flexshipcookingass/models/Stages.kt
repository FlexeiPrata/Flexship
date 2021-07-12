package com.flexship.flexshipcookingass.models

import android.os.Parcelable
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.flexship.flexshipcookingass.other.LOG_ID
import java.io.Serializable


@Entity(tableName = "stages_table")
data class Stages(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String = "",
    var time: Long = 0L,
    val dishId: Int = 0,
    var isCooking:Boolean=false
){
//    override fun equals(other: Any?): Boolean {
//
//        if(javaClass!=other?.javaClass){
//            return false
//        }
//        other as Stages
//        if(id!=other.id)
//            return false
//        if(name!=other.name){
//            Log.d(LOG_ID,"HUI")
//            return false
//        }
//        if(time!=other.time)
//            return false
//        if(dishId!=other.dishId)
//            return false
//        if(isCooking!=other.isCooking)
//            return false
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = id
//        result = 31 * result + name.hashCode()
//        result = 31 * result + time.hashCode()
//        result = 31 * result + dishId
//        result = 31 * result + isCooking.hashCode()
//        return result
//    }
}
