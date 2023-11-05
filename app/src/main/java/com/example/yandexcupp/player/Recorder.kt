package com.example.yandexcupp.player

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

class Recorder(private val context: Context) {

    private var recorder: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRecording = false
    private val bufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val shortBuffer = ShortArray(bufferSize / 2)
    val byteBuffer = ByteBuffer.allocate(bufferSize)



    fun startRecording(onStartSaving: () -> Unit, onEnd: (File) -> Unit, onValue: (Float) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val pcmData = ByteArrayOutputStream()
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        recordingJob = CoroutineScope(Dispatchers.Default).launch {
            recorder?.startRecording()
            while (isActive) {
                val readResult = recorder?.read(shortBuffer, 0, shortBuffer.size) ?: 0
                if (readResult > 0) {
                    byteBuffer.asShortBuffer().put(shortBuffer, 0, readResult)
                    pcmData.write(byteBuffer.array(), 0, readResult * 2)
                    byteBuffer.clear()
                    onValue(abs(shortBuffer.maxOrNull()?.toFloat() ?: 0f))
                }
            }

            recorder?.stop()
            recorder?.release()
            recorder = null

            onStartSaving()

            val pcmBytes = pcmData.toByteArray()
            val wavFile = createWavFile(pcmBytes)
            onEnd(wavFile)
        }


    }

    fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
    }


    private fun createWavFile(pcmBytes: ByteArray): File {

        val fileName = "Recording_${System.currentTimeMillis()}.wav"

//        val values = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//            put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
//            }
//        }
//
//        val resolver = context.contentResolver
//        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
//        val wavFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
//
//        uri?.let {
//            resolver.openOutputStream(it).use { outputStream ->
//                val wavBytes = pcmToWavBytes( pcmBytes)
//                outputStream?.write(wavBytes)
//            }
//        }

        val cacheFile = File(context.filesDir, fileName)

        FileOutputStream(cacheFile).use { outputStream ->
            val wavBytes = pcmToWavBytes(pcmBytes)
            outputStream.write(wavBytes)
        }

        return cacheFile
    }


    private fun pcmToWavBytes(pcmData: ByteArray): ByteArray {
        val sampleRate = 44100
        val channels = 2
        val bitsPerSample = 16
        val headerSize = 44
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = (channels * bitsPerSample / 8).toShort()

        val totalDataLen = pcmData.size + headerSize - 8
        val audioDataSize = pcmData.size

        val buffer = ByteBuffer.allocate(headerSize + audioDataSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        // Write the WAV file header
        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalDataLen)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Sub-chunk size, 16 for PCM
        buffer.putShort(1) // Audio format, 1 for PCM
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign)
        buffer.putShort(bitsPerSample.toShort())
        buffer.put("data".toByteArray())
        buffer.putInt(audioDataSize)

        buffer.put(pcmData)

        return buffer.array()
    }

    fun release() {
        recorder?.release()
    }


}


class VisRecorder(private val context: Context) {

    private var recorder: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRecording = false
    private val bufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val shortBuffer = ShortArray(bufferSize / 2)
    val byteBuffer = ByteBuffer.allocate(bufferSize)


    fun visualize(onValue: (Float) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        recordingJob = CoroutineScope(Dispatchers.Default).launch {
            recorder?.startRecording()
            while (isActive) {
                val readResult = recorder?.read(shortBuffer, 0, shortBuffer.size) ?: 0
                if (readResult > 0) {
                    onValue(abs(shortBuffer.maxOrNull()?.toFloat() ?: 0f))
                }
            }

            recorder?.stop()
            recorder?.release()
            recorder = null

        }

    }

    fun stopVisualize() {
        recordingJob?.cancel()
        recordingJob = null
    }


}




