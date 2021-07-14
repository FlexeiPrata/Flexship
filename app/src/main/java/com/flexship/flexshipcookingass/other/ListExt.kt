package com.flexship.flexshipcookingass.other

fun <T> List<T>.copyList() :MutableList<T>{
    val copyList=this
    return mutableListOf<T>().apply {
        addAll(copyList)
    }
}