package com.example.yandexcupp.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class MultiTrackPlayer(val context: Context) {
    private val tracks = mutableMapOf<Int, AudioTrack>()
    private val trackData = mutableMapOf<Int, ByteArray>()
    private val playingTracks = mutableMapOf<Int, Boolean>()
    private var isRecording = false
    private var recordFile: String? = null
    private val jobList = mutableMapOf<Int,Job>()
    private val scope = CoroutineScope(Dispatchers.Default)


    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val audioFormat = AudioFormat.Builder()
        .setSampleRate(44100)
        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .build()

    private val minBufferSize = AudioTrack.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    fun loadTrack(resId: Int) {
        if(tracks[resId] == null) {
            val fd = context.resources.openRawResourceFd(resId)

            val audioData = ByteArray(fd.length.toInt())
            val fis = FileInputStream(fd.fileDescriptor)
            val offset = fd.startOffset
            fis.skip(offset)
            fis.read(audioData, 0, audioData.size)
            fis.close()

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()


//            audioTrack.write(audioData, 0, audioData.size)

            trackData[resId] = audioData
            tracks[resId] = audioTrack
            playingTracks[resId] = false
        }
    }

    fun loadTrackFromFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            val fis = FileInputStream(file)

            val audioData = ByteArray(file.length().toInt())
            fis.read(audioData)
            fis.close()

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack.write(audioData, 0, audioData.size)
            tracks[file.hashCode()] = audioTrack
        }
    }

    fun loadTracks(resources: List<Int>) {
        resources.forEach { resId ->
            loadTrack(resId)
        }
    }



    fun playTrack(id: Int) {


        val audioTrack = tracks[id]
        audioTrack?.play()
        val job = scope.launch {
            val audioData = trackData[id]
            while (isActive){
                audioTrack?.write(audioData!!, 0, audioData.size)
                audioTrack?.play()
            }
        }
        jobList[id] = job
    }

    fun stopTrack(id: Int) {
        jobList[id]?.cancel()
        tracks[id]?.stop()
        playingTracks[id] = false
//        tracks[id]?.reloadStaticData() // Если вы хотите начать воспроизведение сначала
    }

    fun setVolume(id: Int, volume: Float) {
        tracks[id]?.setVolume(volume)
    }

    fun setPlaybackRate(id: Int, rate: Int) {
        tracks[id]?.playbackRate = rate
    }

}

//class LoopingAudioPlayer(private val audioData: ByteArray, audioAttributes: AudioAttributes) {
//
//
//    private val audioTrack: AudioTrack = AudioTrack.Builder()
//        .setAudioAttributes(audioAttributes)
//        .setAudioFormat(audioFormat)
//        .setBufferSizeInBytes(minBufferSize)
//        .setTransferMode(AudioTrack.MODE_STREAM)
//        .build()
//    private val scope = CoroutineScope(Dispatchers.IO)
//    private var isPlaying = false
//
//    fun start() {
//        isPlaying = true
//        audioTrack.play()
//        scope.launch {
//            while (isPlaying) {
//                audioTrack.write(audioData, 0, audioData.size)
//            }
//        }
//    }
//
//    fun stop() {
//        isPlaying = false
//        audioTrack.stop()
//    }
//
//    companion object {
//        const val SAMPLE_RATE = 44100
//    }
//}
