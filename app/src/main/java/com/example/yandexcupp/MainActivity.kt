package com.example.yandexcupp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yandexcupp.ui.theme.YandexCupPTheme
import com.example.yandexcupp.util.ysFontFamily
import com.example.yandexcupp.view.block.RecordBlock
import com.example.yandexcupp.view.block.RowButtons
import com.example.yandexcupp.view.element.CustomHorizontalSlider
import com.example.yandexcupp.view.element.CustomVerticalSlider
import com.example.yandexcupp.view.element.LoadingView
import com.example.yandexcupp.view.view_model.MainViewModel
import com.example.yandexcupp.view.view_model.ViewStateClass
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {

            var allPermissionsGranted by remember { mutableStateOf(false) }

            val recordAudioLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
            ) { permissionsMap ->
                allPermissionsGranted = permissionsMap.values.all { it }
            }

            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

            LaunchedEffect(key1 = recordAudioLauncher) {
                recordAudioLauncher.launch(
                    permissions
                )
            }


            val vm = hiltViewModel<MainViewModel>()
            val state by vm.state.collectAsState()

            if (allPermissionsGranted) {

                val gradient =
                    Brush.verticalGradient(
                        listOf(
                            Color.Black,
                            Color(0xFF5A50E1),

                            ),
                    )

//            val currentSample: Sample? by vm.curSample.collectAsState()
                val curRate by remember {
                    vm.curRate
                }
                val curVolume by remember {
                    vm.curVolume
                }
                var showSliders by remember {
                    mutableStateOf(true)
                }

                val finalTrack by vm.finalTrack.collectAsState()

                var showDialog by remember(finalTrack) {
                    mutableStateOf(finalTrack.isNotEmpty())
                }

//            LaunchedEffect(key1 = currentSample) {
//                if (currentSample != null) {
//                    vm.trackPlayer.loadTrack(currentSample!!.id, currentSample!!.path)
//                }
//            }

                DisposableEffect(key1 = Unit) {
                    onDispose {
                        vm.release()
                    }
                }

                val context = LocalContext.current


                YandexCupPTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {

                        if (showDialog) {
                            DialogYourChoice(
                                onDismissRequest = { showDialog = false },
                                onShare = {
                                    showDialog = false
                                    vm.shareFile(context)

                                },
                                onSave = {
                                    showDialog = false
                                    vm.saveToDownload(context = context)

                                },
                                onDelete = {
                                    vm.deleteFinalTrack()
                                }) {
                                showDialog = false
                            }
                        }

                        Crossfade(targetState = state, label = "") { curSt ->
                            when (curSt) {
                                ViewStateClass.Loading -> LoadingView()
                                is ViewStateClass.Error -> Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = curSt.e.message ?: "Неизвестная ошибка")
                                }

                                is ViewStateClass.Data -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black)
                                            .padding(12.dp)
                                    ) {

                                        Spacer(modifier = Modifier.weight(2f))

                                        Box(
                                            modifier = Modifier
                                                .weight(8f)
                                                .fillMaxWidth()
                                                .background(gradient)
                                        ) {
                                            if (showSliders) {
                                                CustomVerticalSlider(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .padding(vertical = 12.dp),
                                                    curVolume
                                                ) { value ->
                                                    vm.setVolume(value)

                                                }
                                                CustomHorizontalSlider(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .align(Alignment.BottomCenter)
                                                        .padding(start = 12.dp, end = 2.dp),
                                                    curRate
                                                ) { value ->
                                                    vm.setRate(value)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.weight(2f))
                                    }
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        RowButtons(modifier = Modifier.fillMaxWidth(),
                                            onEnd = { sample ->
                                                vm.onEnd(sample)
                                            }, onInput = { sample ->
                                                vm.onInput(sample)
                                            })
                                    }

                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp)
                                    ) {
                                        RecordBlock(
                                            vm,
                                            onExpand = {
                                                showSliders = !it
                                            },
                                            onMuteLayerBtn = { layer, isMute ->
                                                vm.muteLayer(layer, isMute)
                                            },
                                            onPlayLayerBtn = { layer, isPlay ->
                                                vm.pausePlayLayer(layer, isPlay)
                                            },
                                            onDeleteLayer = {
                                                vm.deleteLayer(it)
                                            },
                                            onSelectLayer = {
                                                vm.selectLayer(it)
                                            },
                                            onPlayAll = { isRecord ->
                                                showSliders = false
                                                if (isRecord) {
                                                    vm.playAllWithRecord()
                                                } else {
                                                    vm.playAllLayers()
                                                }
                                            },
                                            onStopAll = { isRecord ->
                                                showSliders = true
                                                if (isRecord) {
                                                    vm.stopAllWithRecord()
                                                } else {
                                                    vm.stopAllTracks()
                                                }
                                            },
                                            onMicRec = { isRecord ->
                                                if (isRecord) {
                                                    showSliders = false
                                                    vm.startDict()
                                                } else {
                                                    showSliders = true
                                                    vm.stopDict()
                                                }
                                            },
                                            onShare = {
                                                showDialog = true
                                            }
                                        )
                                    }
                                }

                            }

                        }
                    }
                }
            } else {
                Text("Необходимо предоставить разрешения для работы приложения")
                Button(onClick = { recordAudioLauncher.launch(permissions) }) {
                    Text("Предоставить разрешения")
                }
            }
        }
    }
}


@Composable
fun DialogYourChoice(
    onDismissRequest: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "Сделайте Выбор",
                fontSize = 21.sp,
                fontFamily = ysFontFamily,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight(500)
            )

            Divider()

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                onClick = { onShare() }) {
                Text(
                    text = "Поделиться",
                    fontFamily = ysFontFamily,
                )
            }
            Button(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp), onClick = onSave) {
                Text(
                    text = "Сохранить в загрузки",
                    fontFamily = ysFontFamily,
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                onClick = onDelete
            ) {
                Text(
                    text = "Удалить",
                    fontFamily = ysFontFamily,
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                onClick = onCancel
            ) {
                Text(
                    text = "Отмена",
                    fontFamily = ysFontFamily,
                )
            }
        }
    }
}



