package com.example.yandexcupp.view.view_model

sealed class ViewStateClass<out T>{

    object Loading : ViewStateClass<Nothing>()
    data class Error(val e: Exception) : ViewStateClass<Nothing>()
    data class Data<T>(val data: T) : ViewStateClass<T>()

}