package com.example.yandexcupp.util

import android.content.Context
import android.os.Environment
import java.io.File

fun createFile(context: Context): File {
    val downloadsDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val audioFile = File(downloadsDirectory, "my_audio_${System.currentTimeMillis()}.wav")
    audioFile.createNewFile()
    return audioFile
}