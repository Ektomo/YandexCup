package com.example.yandexcupp.view.block

import android.app.Activity.RESULT_OK
import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun ScreenRecorder() {
    val context = LocalContext.current
    val mediaProjectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    val startForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, result.data!!)
            // Здесь код для настройки VirtualDisplay и начала записи
        }
    }

    fun startScreenRecording() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        startForResult.launch(captureIntent)
    }

}