package com.example.yandexcupp.view.view_model

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yandexcupp.data.FinalTrack
import com.example.yandexcupp.data.Layer
import com.example.yandexcupp.data.MicLayer
import com.example.yandexcupp.data.Sample
import com.example.yandexcupp.data.SampleEnum
import com.example.yandexcupp.data.SampleLayer
import com.example.yandexcupp.data.dataStore
import com.example.yandexcupp.player.AudioPlayer
import com.example.yandexcupp.player.MultiTrackPlayer
import com.example.yandexcupp.player.TrackPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.util.Date
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

//
//data class SampleSettings(
//    var id: Int,
//    var volume: Float,
//    var rate: Float
//)

@HiltViewModel
class MainViewModel @Inject constructor(
    val trackPlayer: TrackPlayer,
    val multiTrackPlayer: MultiTrackPlayer,
) : ViewModel() {

    private val _state: MutableStateFlow<ViewStateClass<*>> =
        MutableStateFlow(ViewStateClass.Loading)
    val state = _state.asStateFlow()
    var dateStartRec = Date().time
    var dateFinishRec = Date().time
    var timeRec = AtomicLong(0L)
    var recTimerJob: Job? = null
    var track: AudioPlayer? = null

    val layers = mutableStateListOf<Layer>()
    val curSample = MutableStateFlow<Sample?>(null)
    val endTrack = MutableStateFlow(false)
    val curLayer = MutableStateFlow<Layer?>(null)
    val curRate = mutableFloatStateOf(1f)
    val curVolume = mutableFloatStateOf(0.5f)
    val finalTrack = MutableStateFlow("")
    val reco = MutableStateFlow(1)
    private var myJob: Job? = null

    private val _amplitude = MutableStateFlow(0f)
    val amplitude = _amplitude.asStateFlow()


    init {
        loadAllTrack()
    }

    private fun loadAllTrack() {
        _state.update { ViewStateClass.Loading }
        myJob?.cancel()
        myJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                trackPlayer.loadAllTracks(dataStore)
                trackPlayer.loadingAllProcess.collect {
                    if (it.loading) {
                        _state.update { ViewStateClass.Loading }
                    } else {
                        _state.update { ViewStateClass.Data(dataStore) }
                    }
                }
            } catch (e: Exception) {
                _state.update { ViewStateClass.Error(e) }
            }
        }
    }

    fun backFromVis() {
        _state.update { ViewStateClass.Loading }
         viewModelScope.launch(Dispatchers.IO) {
            try {
//                trackPlayer.loadAllTracks(dataStore)
//                trackPlayer.loadingAllProcess.collect {
//                    if (it.loading) {
//                        _state.update { ViewStateClass.Loading }
//                    } else {
                        _state.update { ViewStateClass.Data(dataStore) }
//                    }

            } catch (e: Exception) {
                _state.update { ViewStateClass.Error(e) }
            }
        }
    }

    fun loadAndPlayTrackFromFile(context: Context) {
        _state.update { ViewStateClass.Loading }
        viewModelScope.launch {

            track = AudioPlayer(context)
            track?.loadFromFile(finalTrack.value,{
                endTrack.update { true }
                _state.update { ViewStateClass.VideoPlayerState(finalTrack.value) }
//                playAudioTrack()
            }){

            }
        }
    }

    fun playAudioTrack(){
        track?.play()
    }

    fun stopAudioTrack(){
        track?.stop()
    }

    fun pauseAudioTrack(){
        track?.pause()
    }

    fun goAudioTrackTo(time: Int){
        track?.seekTo(time)
    }

    fun getDurationAudio() = track?.getDuration()
    fun getCurrentPositionAudio() = track?.getPosition()

    fun getToStart() = goAudioTrackTo(0)
    fun goToEnd() {
        val a = getDurationAudio()
        a?.let{goAudioTrackTo(it)}
    }
    fun playTrackFromSoundPool() {

    }

    private fun loadAllTrackMulti() {
        _state.update { ViewStateClass.Loading }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                multiTrackPlayer.loadTracks(dataStore.map { it.id to it.path }) {
                    _state.update { ViewStateClass.Data(dataStore) }
                }
//                multiTrackPlayer.loadingAllProcess.collect {
//                    if (it.loading) {
//                        _state.update { ViewStateClass.Loading }
//                    } else {

//                    }
//                }
            } catch (e: Exception) {
                _state.update { ViewStateClass.Error(e) }
            }
        }
    }

    fun onInput(sample: Sample) {
        curLayer.let {
            it.value?.id?.let { id -> trackPlayer.stopTrack(id) }
            it.update { null }
        }

        if (curSample.value != sample) {
            if (curSample.value != null) {
                trackPlayer.stopTrack(curSample.value!!.id)
            }
        }
        curSample.update { sample }
        trackPlayer.playTrack(sample.id, 0.5f, 1f)
    }

    fun onEnd(sample: Sample?) {
        stopAllTracks()
        if (curSample.value != null) {
            curSample.update { null }
        }
        if (sample != null) {
            val sampleLayer = SampleLayer(sample.sampleEnum, path = sample.path)
            sampleLayer.isPlay = true
            curLayer.update { sampleLayer }
            trackPlayer.loadTrack(sampleLayer.id, sampleLayer.path) {
                trackPlayer.playTrack(sampleLayer.id, sampleLayer.volume, sampleLayer.rate)
                trackPlayer.visualize { value -> _amplitude.update { value } }
            }
//            trackPlayer.loadTrack(sampleLayer.id, sampleLayer.path) {
//                trackPlayer.playTrack(sampleLayer.id, sampleLayer.volume, sampleLayer.rate)
//                trackPlayer.visualize { value -> _amplitude.update { value } }
//            }

            layers.add(sampleLayer)

            curVolume.floatValue = sampleLayer.volume
            curRate.floatValue = sampleLayer.rate
        }
    }

    fun setVolume(value: Float) {
        if (curLayer.value != null) {
            layers.firstOrNull { it.id == curLayer.value?.id }?.volume = value
            curVolume.floatValue = value
            trackPlayer.setVolume(curLayer.value!!.id, value)
        }
    }

    fun muteLayer(layer: Layer, isMute: Boolean) {
        val findingLayer = layers.firstOrNull { it.id == layer.id }
        if (findingLayer != null) {
            findingLayer.isMute = isMute
        }
        trackPlayer.setVolume(layer.id, if (isMute) 0f else layer.volume)
        reco.update { it + 1 }
    }


    fun setRate(value: Float) {
        if (curLayer.value != null) {
            layers.firstOrNull { it.id == curLayer.value?.id }?.rate = value
            curRate.floatValue = value
            trackPlayer.setRate(curLayer.value!!.id, value)
        }
    }

    fun playAllLayers() {
        curLayer.update { null }
        curSample.update { null }
        trackPlayer.stopAllTracks()
        layers.forEach { layer ->
            layer.isPlay = true
            trackPlayer.playTrack(layer.id, if (layer.isMute) 0f else layer.volume, layer.rate)
        }
        reco.update { it + 1 }
        trackPlayer.visualize { value ->
            _amplitude.update { value }
        }
    }


//    fun playWithRecord() {
//        curLayer.update { null }
//        curSample.update { null }
////        layers.forEach { layer ->
////            trackPlayer.playTrack(layer.id, layer.volume, layer.rate)
////        }
//        layers.firstOrNull()?.let { trackPlayer.playWithRecord(it.id) }
//    }
//
//    fun stopWithRecord() {
//        curLayer.update { null }
//        curSample.update { null }
//        layers.firstOrNull()?.let { trackPlayer.stopWithRecord(it.id) }
//    }


    fun stopAllTracks() {
        curLayer.update { null }
        curSample.update { null }
        trackPlayer.stopAllTracks()
        layers.forEach { it.isPlay = false }
        reco.update { it + 1 }
        trackPlayer.stopVisualise()
        _amplitude.update { 0f }
    }

    fun deleteFinalTrack() {
        viewModelScope.launch(Dispatchers.IO) {
            val filePath = finalTrack.value
            if (filePath.isNotEmpty()) {
                File(filePath).run {
                    if (exists()) {
                        finalTrack.update { "" }
                        FinalTrack.path = ""
                        delete()
                    }
                }
            }
        }
    }

    fun pausePlayLayer(layer: Layer, isPlay: Boolean) {

        val findingLayer = layers.firstOrNull { it.id == layer.id }

        if (findingLayer != null) {
            if (!isPlay) {
                layer.isPlay = false
                trackPlayer.stopTrack(layer.id)
                reco.update { it + 1 }
            } else {
                layer.isPlay = true
                trackPlayer.playTrack(layer.id, if (layer.isMute) 0f else layer.volume, layer.rate)
                reco.update { it + 1 }
//                trackPlayer.visualize { value -> _amplitude.update { value } }
            }
        }

    }

    fun deleteLayer(layer: Layer) {
        val findingLayer = layers.firstOrNull { it.id == layer.id }
        if (findingLayer != null) {
            trackPlayer.stopTrack(findingLayer.id)
            trackPlayer.deleteFromSoundPool(findingLayer.id)

            if (findingLayer is MicLayer) {
                val file = File(findingLayer.filePath)
                if (file.exists()) {
                    file.delete()
                }
            }

            layers.remove(findingLayer)

            if (layers.isEmpty()) {
                trackPlayer.stopVisualise()
                _amplitude.update { 0f }
            }
        }

    }

    fun selectLayer(layer: Layer) {
        val findingLayer = layers.firstOrNull { it.id == layer.id }
        if (findingLayer != null) {
            stopAllTracks()
            curRate.floatValue = findingLayer.rate
            curVolume.floatValue = findingLayer.volume
            layer.isPlay = true
            curLayer.update { layer }
            findingLayer.isMute = false
            trackPlayer.playTrack(findingLayer.id, findingLayer.volume, findingLayer.rate)
            trackPlayer.visualize { value -> _amplitude.update { value } }
        }
    }


    fun startDict() {
//        trackPlayer.stopVisualise()
        trackPlayer.stopAllTracks()
//        trackPlayer.stopVisualise()
        trackPlayer.startMic({
            _state.update { ViewStateClass.Loading }
        }, { file ->
            try {

                val newLayer = MicLayer(SampleEnum.Mic, file.absolutePath)

                trackPlayer.loadTrackFromFile(newLayer.id, file) {
                    trackPlayer.playTrack(newLayer.id, newLayer.volume, newLayer.rate)
                    trackPlayer.visualize { float -> _amplitude.update { float } }
                    layers.add(newLayer)
                    newLayer.isPlay = true
                    curLayer.update { newLayer }
                    reco.update { it + 1 }
                    _state.update { ViewStateClass.Data(dataStore) }
                }
//                _amplitude.update { 0f }
            } catch (e: Exception) {
                _state.update { ViewStateClass.Error(e) }
            }
        }) { value ->
            _amplitude.update { value }
        }
    }

    fun stopDict() {
        trackPlayer.stopMic()
        trackPlayer.stopVisualise()
        _amplitude.update { 0f }
    }


    fun playAllWithRecord() {
        playAllLayers()
        dateStartRec = Date().time
        trackPlayer.startRecord(onStartSaving = {
            _state.update { ViewStateClass.Loading }

        }, { file ->
            FinalTrack.path = file.absolutePath
            finalTrack.update { file.absolutePath }
            _state.update { ViewStateClass.Data(dataStore) }
        }) { value ->
            _amplitude.update { value }
        }
    }

    fun stopAllWithRecord() {
        stopAllTracks()
        trackPlayer.stopRecord()
        _amplitude.update { 0f }
    }


    fun defaultPlayTrack(id: String) {
        trackPlayer.playTrack(id, 0.5f, 1f)
    }


    fun saveToDownload(context: Context) {

        if (finalTrack.value.isNotEmpty()) {


            val file = File(finalTrack.value)
            if (file.exists()) {
                val fileName = "Final_${System.currentTimeMillis()}.wav"

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
                val wavFile =
                    File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

                uri?.let {
                    FileInputStream(file).use { inStream ->
                        resolver.openOutputStream(it).use { outputStream ->
                            val buffer = ByteArray(1024)
                            var length: Int
                            while (inStream.read(buffer).also { length = it } > 0) {
                                outputStream?.write(buffer, 0, length)
                            }
                        }
                    }
                }
            }
        }
    }


    fun shareFile(context: Context) {

        if (finalTrack.value.isNotEmpty()) {

            val f = File(finalTrack.value)

            if (f.exists()) {

                val fileUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    f
                )

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    type = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooserIntent = Intent.createChooser(shareIntent, null)
                chooserIntent.resolveActivity(context.packageManager)?.let {
                    context.startActivity(chooserIntent)
                }
            }
        }
    }

    fun release() {
        trackPlayer.release()
    }

}