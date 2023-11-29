package com.example.yandexcupp.player

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicLong

class AudioPlayer(private val context: Context) {

    private var sampleRate = 44100 // Стандартная частота дискретизации
    private var isPlaying = false
    private val jobLoad = Job()
    private val scope = CoroutineScope(Dispatchers.IO + jobLoad)
    private var jobPlay: Job? = null
//    private val scopePlay = CoroutineScope(Dispatchers.IO + jobPlay)
    private var volume: Float = 0.5f
    private var rate: Int = 1
    private var repeatIntervalMillis = AtomicLong(100)



    private var audioTrack: AudioTrack? = null
//    init {
//        if (resourceId != null){
//            val afd = context.resources.openRawResourceFd(resourceId)
//            loadAudioTrack(afd.createInputStream(), afd.length.toInt())
//            afd.close()
//        }
//        if (filePath != null){
//            val fis = FileInputStream(filePath)
//            loadAudioTrack(fis, fis.channel.size().toInt())
//        }
//    }
    fun loadFromResource(resourceId: Int, onComplete: () -> Unit) {
//        scope.launch {
            val afd = context.resources.openRawResourceFd(resourceId)
            loadAudioTrack(afd.createInputStream(), afd.length.toInt())
            afd.close()
//            withContext(Dispatchers.Main) {
                onComplete()
//            }
//        }
    }

    fun loadFromFile(filePath: String, onComplete: () -> Unit) {
//        scope.launch {
            val fis = FileInputStream(filePath)
            loadAudioTrack(fis, fis.channel.size().toInt())
            fis.close()
//            withContext(Dispatchers.Main) {
                onComplete()
//            }
//        }
    }

    private fun loadAudioTrack(inputStream: InputStream, size: Int) {
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            buffer.size,
            AudioTrack.MODE_STATIC
        )
        audioTrack?.write(buffer, 0, buffer.size)
    }

    fun play() {
        audioTrack?.play()
        isPlaying = true
    }

    fun playSample() {
        jobPlay?.cancel()
        jobPlay = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                audioTrack?.play()
                delay(repeatIntervalMillis.get())
                audioTrack?.stop()
                audioTrack?.reloadStaticData()
            }
        }
    }


    fun stop() {
        audioTrack?.stop()
//        audioTrack?.release()
        jobPlay?.cancel()
        isPlaying = false
    }

    fun setVolume(volume: Float) {
        this.volume = volume
        audioTrack?.setVolume(volume)
    }




    fun setPlaybackRate(playbackRate: Int) {
        this.rate = playbackRate
        audioTrack?.playbackRate = playbackRate
    }

    fun setRepeatInterval(value: Long){
        this.repeatIntervalMillis.set(value)
    }

    fun isPlaying() = isPlaying

}