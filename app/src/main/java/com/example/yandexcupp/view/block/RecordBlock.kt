package com.example.yandexcupp.view.block

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yandexcupp.data.Layer
import com.example.yandexcupp.util.ysFontFamily
import com.example.yandexcupp.view.view_model.MainViewModel
import kotlin.math.sin


//@Preview
//@Composable
//fun PreviewBox() {
//    BoxWithConstraints(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .background(Color.Black)
//    ) {
//        RecordBlock()
//    }
//}

//val layerList = listOf(
//    SampleLayer(SampleEnum.Drum, 1),
//    SampleLayer(SampleEnum.Flute, 2),
//    SampleLayer(SampleEnum.Drum, 1),
//    SampleLayer(SampleEnum.Flute, 2),
//    SampleLayer(SampleEnum.Drum, 1),
//    SampleLayer(SampleEnum.Flute, 2),
//    SampleLayer(SampleEnum.Drum, 1),
//    SampleLayer(SampleEnum.Flute, 2),
//            SampleLayer(SampleEnum.Drum, 1),
//SampleLayer(SampleEnum.Flute, 2),
//SampleLayer(SampleEnum.Drum, 1),
//SampleLayer(SampleEnum.Flute, 2),
//SampleLayer(SampleEnum.Drum, 1),
//SampleLayer(SampleEnum.Flute, 2),
//SampleLayer(SampleEnum.Drum, 1),
//SampleLayer(SampleEnum.Flute, 2)
//)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BoxWithConstraintsScope.RecordBlock(
    viewModel: MainViewModel,
    onExpand: (Boolean) -> Unit,
    onPlayLayerBtn: (Layer, Boolean) -> Unit,
    onMuteLayerBtn: (Layer, Boolean) -> Unit,
    onDeleteLayer: (Layer) -> Unit,
    onSelectLayer: (Layer) -> Unit,
    onPlayAll: (Boolean) -> Unit,
    onStopAll: (Boolean) -> Unit,
    onMicRec: (Boolean) -> Unit,
    onShare: () -> Unit
) {

    val layerList = remember { viewModel.layers }


    var isExpanded by remember { mutableStateOf(false) }
    val transition = updateTransition(isExpanded, label = "button state")
    val expandColor by transition.animateColor(label = "expand color") {
        if (it) Color(0xFFA7DA10) else Color.White
    }


    val isFinalTrack by viewModel.finalTrack.collectAsState()

    val curLayer by viewModel.curLayer.collectAsState()

    var playAll by remember {
        mutableStateOf(curLayer != null)
    }
    var recAll by remember {
        mutableStateOf(false)
    }

    val amplitude by viewModel.amplitude.collectAsState()

    var recMic by remember {
        mutableStateOf(false)
    }

    val height = maxHeight

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(maxHeight = height * 0.7f)
            .align(Alignment.BottomCenter)
    ) {

        transition.AnimatedVisibility(visible = { it }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(BottomCenter)
                    .padding(bottom = 96.dp)
                    .verticalScroll(
                        rememberScrollState()
                    ),
                verticalArrangement = Arrangement.Center
            ) {


                layerList.forEach { layer ->

                    var isPlaying by remember(curLayer) {
                        mutableStateOf(curLayer != null && curLayer?.id == layer.id)
                    }
                    var isMute by remember {
                        mutableStateOf(layer.volume == 0f)
                    }


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                            .background(Color.Transparent, RoundedCornerShape(4.dp)),
                        verticalAlignment = CenterVertically
                    ) {
                        Row(
                            Modifier
                                .weight(7f)
                                .background(Color.White, RoundedCornerShape(4.dp, 0.dp, 0.dp, 4.dp))
                                .clickable {
                                    if (!recAll) {
                                        isExpanded = !isExpanded
                                        onExpand(isExpanded)
                                        playAll = false
                                        onSelectLayer(layer)
                                    }
                                }
                                .padding(start = 4.dp),
                            horizontalArrangement = SpaceBetween,
                            verticalAlignment = CenterVertically
                        ) {
                            Text(
                                text = layer.id,
                                fontSize = 14.sp,
                                fontFamily = ysFontFamily,
                            )
                            Row(modifier = Modifier) {
                                IconButton(
                                    onClick = {
                                        isPlaying = !isPlaying
                                        onPlayLayerBtn(layer, isPlaying)
                                    },
                                    modifier = Modifier
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "",
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        isMute = !isMute
                                        onMuteLayerBtn(layer, isMute)
                                    },
                                    modifier = Modifier
                                ) {
                                    Icon(
                                        imageVector = if (isMute) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = ""
                                    )
                                }
                            }
                        }
                        FilledIconButton(
                            shape = RoundedCornerShape(0.dp, 4.dp, 4.dp, 0.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color(
                                    0XFFE4E4E4
                                )
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                            onClick = {
                                onDeleteLayer(layer)
                            }) {
                            Icon(Icons.Default.Close, "")
                        }

                    }

                    Spacer(modifier = Modifier.padding(vertical = 4.dp))

                }
            }
        }





        Column(Modifier.fillMaxWidth().align(BottomCenter)) {

            Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                val lineCount = 50
                val lineSpacing = size.width / (lineCount - 1)
                val waveHeight = (amplitude / Short.MAX_VALUE.toFloat()) * size.height / 2

                for (i in 0 until lineCount) {
                    val x = lineSpacing * i
                    val yOffset = sin(i * 0.3f) * waveHeight
                    val startY = size.height / 2 + yOffset
                    val endY = size.height / 2 - yOffset

                    drawLine(
                        color = Color(0xFFA7DA10),
                        start = Offset(x, startY),
                        end = Offset(x, endY),
                        strokeWidth = 4.dp.toPx()
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    ,
                horizontalArrangement = SpaceBetween,
                verticalAlignment = CenterVertically
            ) {
                Button(
                    onClick = {
                        if (!recMic) {
                            isExpanded = !isExpanded
                            onExpand(isExpanded)
                        }
                    },
                    enabled = !recMic,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = expandColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Слои",
                        fontSize = 14.sp,
                        fontFamily = ysFontFamily,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight(400)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = ""
                    )
                }

                Row(verticalAlignment = CenterVertically) {
                    FilledIconButton(
                        onClick = {
                            recMic = !recMic
                            onMicRec(recMic)
                            isExpanded = false
                        },
                        shape = RoundedCornerShape(4.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "",
                            modifier = Modifier,
                            tint = if (recMic) Color.Red else Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 1.dp))
                    FilledIconButton(
                        onClick = {
                            if (!recAll) {
                                onPlayAll(true)
                            } else onStopAll(true)
                            recAll = !recAll
                        },
                        shape = RoundedCornerShape(4.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = "",
                            modifier = Modifier,
                            tint = if (recAll) Color.Red else Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 1.dp))
                    FilledIconButton(
                        onClick = {
                            if (!playAll) {
                                onPlayAll(false)
                            } else onStopAll(false)
                            playAll = !playAll
                        },
                        shape = RoundedCornerShape(4.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                    ) {
                        Icon(
                            imageVector = if (playAll) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "",
                            modifier = Modifier
                        )
                    }

                    if (isFinalTrack.isNotEmpty()) {
                        FilledIconButton(
                            onClick = {
                                onShare()
                            },
                            shape = RoundedCornerShape(4.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "",
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
        }


    }


}