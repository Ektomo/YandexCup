package com.example.yandexcupp.util

fun Short.reverseBytes(shortVal: Short): Short {
    val low = (shortVal.toInt() and 0xFF00) ushr 8
    val high = (shortVal.toInt() and 0x00FF) shl 8
    return (high or low).toShort()
}