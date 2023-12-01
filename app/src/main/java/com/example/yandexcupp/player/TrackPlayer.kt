package com.example.yandexcupp.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.core.content.FileProvider
import com.example.yandexcupp.data.Sample
import com.example.yandexcupp.data.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.IOException


data class TrackPlayerProcess(
    val loading: Boolean,
)

class TrackPlayer(val context: Context, val maxStreams: Int) {

    private val recorder = Recorder(context)
    private val visRecorder = VisRecorder(context)
    private lateinit var soundPool: SoundPool

    init {
        buildSoundPool(maxStreams)
    }

    private fun buildSoundPool(streams: Int) {
        if (this::soundPool.isInitialized){
            soundPool.release()
        }
        soundPool = SoundPool.Builder()
            .setMaxStreams(streams)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    fun changeMaxStreams(newMaxStreams: Int) {
        buildSoundPool(newMaxStreams)
        loadAllTracks(dataStore)
    }

    private var mediaPlayerMap = mutableMapOf<String, MediaPlayer>()

    private val soundIds = mutableMapOf<String, Int>()
    private val activeStreams = mutableMapOf<String, Int>()
    private val wasAllLoaded = mutableMapOf<Int, Int>()

    val loadingAllProcess = MutableStateFlow(TrackPlayerProcess(false))


    fun loadTrack(id: String, resourceId: Int, onEndLoad: () -> Unit = {}) {
        if (soundIds[id] == null) {
            val soundId = soundPool.load(context, resourceId, 1)
            soundIds[id] = soundId
        }
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                onEndLoad()
            }
        }
    }

    fun visualize(onValue: (Float) -> Unit) {
        visRecorder.visualize { onValue(it) }
    }

    fun stopVisualise() {
        visRecorder.stopVisualize()
    }


    fun playMusicFromFile(id: String, file: File, loop: Boolean = true) {
        val uri = FileProvider.getUriForFile(context, "com.example.yandexcupp.provider", file)
        var mediaPlayer = mediaPlayerMap[id]
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayerMap[id] = mediaPlayer
        }
        mediaPlayer.release()
        mediaPlayer.apply {
            try {
                setDataSource(context, uri)
                prepare()
                isLooping = loop
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun playMusic(id: String, resourceId: Int, loop: Boolean = true) {
        var mediaPlayer = mediaPlayerMap[id]
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayerMap[id] = mediaPlayer
        }
        mediaPlayer?.release()
        mediaPlayer?.apply {
            isLooping = loop
            start()
        }
    }

    fun pauseMusic(id: String) {
        mediaPlayerMap[id]?.apply {
            pause()
        }
    }

    fun resumeMusic(id: String) {
        mediaPlayerMap[id]?.apply {
            start()
        }
    }

    fun stopMusic(id: String) {
        mediaPlayerMap[id]?.apply {
            stop()
        }
        mediaPlayerMap.remove(id)
    }

    fun loadTrackFromFile(id: String, file: File, onEndLoad: () -> Unit) {
        if (soundIds[id] == null) {
            val soundId = soundPool.load(file.absolutePath, 1)
            soundIds[id] = soundId
        }
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                onEndLoad()
            }
        }
    }

    fun loadAllTracks(list: List<Sample>) {
        loadingAllProcess.update { it.copy(loading = true) }

//        if (wasAllLoaded.isEmpty()) {

        wasAllLoaded.clear()
        soundIds.forEach { soundPool.unload(it.value) }
        soundIds.clear()
        soundPool.release()
        soundPool = SoundPool.Builder()
            .setMaxStreams(maxStreams)
            .build()
        list.forEach { sample ->
            if (soundIds[sample.id] == null) {
                val soundId = soundPool.load(context, sample.path, 1)
                wasAllLoaded[soundId] = 0
                soundIds[sample.id] = soundId
            }
        }
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                wasAllLoaded[sampleId] = 1
            }
            if (!wasAllLoaded.contains(0)) {
                loadingAllProcess.update { it.copy(loading = false) }
            }
        }
//        }
//    else{
//            loadingAllProcess.update { it.copy(loading = false) }
//        }


    }

    fun playTrack(id: String, volume: Float, rate: Float) {
        if (!activeStreams.containsKey(id)) {
            val streamId = soundPool.play(soundIds[id] ?: return, volume, volume, 1, -1, rate)
            activeStreams[id] = streamId
        }
    }


    fun setVolume(id: String, volume: Float) {
        soundPool.setVolume(activeStreams[id] ?: return, volume, volume)
    }

    fun setRate(id: String, rate: Float) {
        soundPool.setRate(activeStreams[id] ?: return, rate)
    }

    fun stopTrack(id: String) {
        soundPool.stop(activeStreams[id] ?: return)
        activeStreams.remove(id)
    }

    fun deleteFromSoundPool(id: String) {
        soundPool.unload(activeStreams[id] ?: return)
        activeStreams.remove(id)
        soundIds.remove(id)
    }

    fun pauseTrack(id: String) {
        soundPool.pause(activeStreams[id] ?: return)
    }

    fun resumeTrack(id: String) {
        soundPool.resume(activeStreams[id] ?: return)
    }


//    fun playWithRecord(id: String) {
//        playTrack(id, 1f, 1f)
//        recorder.startRecording({}) {}
//    }
//
//    fun stopWithRecord(id: String) {
//        stopTrack(id)
//        recorder.stopRecording()
//    }

    fun startRecord(onStartSaving: () -> Unit, onEnd: (File) -> Unit, onValue: (Float) -> Unit) {
        recorder.startRecording(onStartSaving = onStartSaving, onEnd = onEnd, onValue = onValue)
    }

    fun stopRecord() {
        recorder.stopRecording()
    }

    fun startMic(onStartSaving: () -> Unit, onEnd: (File) -> Unit, onValue: (Float) -> Unit) {
        stopAllTracks()
        recorder.startRecording(onStartSaving = onStartSaving, onEnd = onEnd, onValue = onValue)
    }

    fun stopMic() {
        recorder.stopRecording()
    }

    fun playAllTracks() {
        activeStreams.forEach {
            activeStreams[it.key]?.let { ast -> soundPool.stop(ast) }
        }
        activeStreams.clear()
    }

    fun stopAllTracks() {
        activeStreams.forEach {
            activeStreams[it.key]?.let { ast -> soundPool.stop(ast) }
        }
        activeStreams.clear()
    }

    fun currentPlayingTrackId(): String? {
        return if (activeStreams.size == 1) {
            activeStreams.map { it.key }.firstOrNull()
        } else {
            null
        }
    }

    fun release() {
        soundPool.release()
        mediaPlayerMap.values.forEach { it.release() }
        recorder.release()
    }
}