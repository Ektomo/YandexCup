package com.example.yandexcupp.player

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicLong

class AudioPlayer(private val context: Context) {

    private var sampleRate = 44100 // Стандартная частота дискретизации
    private var isPlaying = false
    private val jobLoad = Job()

    private var jobPlay: Job = Job()
    private val scope = CoroutineScope(Dispatchers.Default+ jobPlay)

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

        context.resources.openRawResourceFd(resourceId).use { afd ->
            loadAudioTrack(afd.createInputStream(), afd.length.toInt())
        }
        onComplete()
    }
    fun loadAndPlayFromResource(resourceId: Int) {

        context.resources.openRawResourceFd(resourceId).use { afd ->
            val buffer = ByteArray(afd.length.toInt())
            val inSt = afd.createInputStream()
            inSt.read(buffer)
            inSt.close()
            playWithDynamicPauses(buffer)
//            loadAudioTrack(afd.createInputStream(), afd.length.toInt())
        }
//        onComplete()
    }

    fun loadFromFile(filePath: String, onComplete: () -> Unit) {
//        scope.launch {
        FileInputStream(filePath).use { fis ->

            loadAudioTrack(fis, fis.channel.size().toInt())
        }

//            withContext(Dispatchers.Main) {
        onComplete()
//            }
//        }
    }

//    private fun loadAudioTrack(inputStream: InputStream, size: Int) {
//        val buffer = ByteArray(size)
//        inputStream.read(buffer)
//        inputStream.close()
//
//        audioTrack = AudioTrack(
//            AudioManager.STREAM_MUSIC,
//            sampleRate,
//            AudioFormat.CHANNEL_OUT_MONO,
//            AudioFormat.ENCODING_PCM_16BIT,
//            buffer.size,
//            AudioTrack.MODE_STATIC
//        )
//        audioTrack?.write(buffer, 0, buffer.size)
//    }

    private val silenceDurationInSec = 1
    fun playWithPauses(sampleRate: Int, audioData: ByteArray, silenceDurationInSec: Int) {
        val bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        val silence = ByteArray(sampleRate * silenceDurationInSec * 2)

        jobPlay = scope.launch {
            audioTrack?.play()

            while (isActive) {
                audioTrack?.write(audioData, 0, audioData.size)
                audioTrack?.write(silence, 0, silence.size) // вставляем тишину
            }
        }
    }

    fun play() {
        audioTrack?.play()
        isPlaying = true
    }

    fun playSample() {
        jobPlay.cancel()
        jobPlay = scope.launch {
            while (isActive) {
                audioTrack?.play()
                delay(repeatIntervalMillis.get())
                audioTrack?.stop()
                audioTrack?.reloadStaticData()
            }
        }
    }


    @Volatile
    private var pauseDurationMs = 1000L

    fun loadAudioTrack(inputStream: InputStream, size: Int) {
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            buffer.size,
            AudioTrack.MODE_STREAM
        )
        audioTrack?.write(buffer, 0, buffer.size)
    }

    fun playWithDynamicPauses(audioData: ByteArray) {
        audioTrack?.play()
        isPlaying = true

        jobPlay.cancel() // Отменяем предыдущий Job, если он существует
        jobPlay = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && isPlaying) {
                audioTrack?.write(audioData, 0, audioData.size)

                val silenceDurationInFrames = pauseDurationMs * sampleRate / 1000
                val silence = ByteArray(silenceDurationInFrames.toInt() * 2) // 2 байта на сэмпл для 16-бит PCM
                audioTrack?.write(silence, 0, silence.size) // вставляем тишину
            }
        }
    }

    fun setPauseDuration(newPauseDurationMs: Long) {
        pauseDurationMs = newPauseDurationMs
    }


    fun stop() {
        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack?.stop()
        }
        audioTrack?.release()
        jobPlay.cancel()
        isPlaying = false
    }

    fun release(){
        stop()
        audioTrack?.release()
    }

    fun setVolume(volume: Float) {
        this.volume = volume
        audioTrack?.setVolume(volume)
    }


    fun setPlaybackRate(playbackRate: Int) {
        this.rate = playbackRate
        audioTrack?.playbackRate = playbackRate
    }

    fun setRepeatInterval(value: Long) {
        this.repeatIntervalMillis.set(value)
    }

    fun isPlaying() = isPlaying

}