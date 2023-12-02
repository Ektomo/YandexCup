package com.example.yandexcupp.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yandexcupp.R
import com.example.yandexcupp.util.interFontFamily
import com.example.yandexcupp.view.view_model.MainViewModel
import kotlinx.coroutines.delay
import java.lang.Math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random


@Composable
fun VideoPlayer(
    file: String,
    vm: MainViewModel,
) {


    LaunchedEffect(key1 = Unit) {
        vm
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row {
                FilledIconButton(shape = RoundedCornerShape(4.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(
                            0XFF5A50E2
                        )
                    ),
                    modifier = Modifier.padding(end = 12.dp)
//                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ,
                    onClick = {
                        vm.backFromVis()
                    }) {
                    Icon(Icons.Default.ArrowBack, "")
                }
                Text(
                    text = "Название трека",
                    fontFamily = interFontFamily,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
            FilledIconButton(shape = RoundedCornerShape(4.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(
                        0XFFA8DB10
                    )
                ),
                modifier = Modifier
//                    .padding(end = 12.dp)
//                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ,
                onClick = {

                }) {
                Icon(Icons.Default.Download, "")
            }


        }


//        var size by remember { mutableStateOf(200.dp) }
//        var initialDistance by remember { mutableStateOf(0f) }
//        var offset by remember { mutableStateOf(Offset(0f, 0f)) }

        MultiplyViewElems()
        Bottom(vm)


//        var offset by remember { mutableStateOf(Offset.Zero) }
////        var zoom by remember { mutableFloatStateOf(1f) }
//        var size by remember {
//            mutableFloatStateOf(100f)
//        }
//        var angle by remember { mutableFloatStateOf(0f) }
//
//
//        BoxWithConstraints(modifier = Modifier.weight(8f)) {
//
//        }

    }


}


fun Offset.rotateBy(angle: Float): Offset {
    val angleInRadians = angle * PI / 180
    return Offset(
        (x * kotlin.math.cos(angleInRadians) - y * kotlin.math.sin(angleInRadians)).toFloat(),
        (x * kotlin.math.sin(angleInRadians) + y * kotlin.math.cos(angleInRadians)).toFloat()
    )
}

val imgs = arrayOf(
    R.drawable.group_1,
    R.drawable.group_2,
    R.drawable.group_3,
    R.drawable.group_4,
    R.drawable.group_5,
    R.drawable.group_6
)


@Composable
fun ColumnScope.MultiplyViewElems() {
    BoxWithConstraints(
        modifier = Modifier
            .weight(8f)
            .fillMaxSize()
    ) {
        for (i in imgs) {


            var offset by remember {
                mutableStateOf(
                    Offset(
                        randomFloatInRange(0f, 300f),
                        randomFloatInRange(0f, 300f)
                    )
                )
            }

            var change by remember {
                mutableStateOf(false)
            }

            val animatedOffsetX by animateFloatAsState(
                targetValue = if (change) 0f else randomFloatInRange(20f, 300f), label = "",
            )

//            val animatedOffsetY by animateFloatAsState(
//                targetValue = if (change) 0f else randomFloatInRange(20f, 300f), label = "",
//            )

            LaunchedEffect(key1 = Unit) {
//                while (!change) {
                if(!change) {
                    while (true) {
                        offset = Offset(offset.x + if(offset.x > 400f) {-1f} else +1f , offset.y  + if(offset.y > 400f) {-1f} else +1f)
                        delay(10)
                    }
                }
//                }
            }
//        var zoom by remember { mutableFloatStateOf(1f) }
            var size by remember {
                mutableFloatStateOf(randomFloatInRange(50f, 300f))
            }
            var angle by remember { mutableFloatStateOf(randomFloatInRange(0f, 180f)) }

            Image(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            val x = offset.x.coerceIn(0f, maxWidth.toPx() - placeable.width)
                            val y = offset.y.coerceIn(0f, maxHeight.toPx() - placeable.height)
                            placeable.placeRelative(x.roundToInt(), y.roundToInt())
                        }
                    }
                    .size(size.dp)
//                    .rotate(angle)
                    .graphicsLayer {
                        rotationZ = angle
                    }
                    .offset(x = offset.x.dp, y = offset.y.dp)

                    .pointerInput(Unit) {
                        detectTransformGesturesAndEnd(
                            onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                                change = true
                                val oldScale = size
                                val newScale = size * gestureZoom
//                                offset += pan

//                                offset = (offset + centroid / oldScale).rotateBy(gestureRotate) -
//                                        (centroid / newScale + pan / oldScale)
////
//
////                            offset += pan
//
                                size = newScale

                                angle += gestureRotate

                            },
                            onGestureEnd = {
                                change = false
                            }
                        )

                    }
//                    .graphicsLayer {
//                        translationX = -offset.x * zoom
//                        translationY = -offset.y * zoom
//                        scaleX = zoom
//                        scaleY = zoom
//                        rotationZ = angle
//                        transformOrigin = TransformOrigin(0f, 0f)
//                    }
                ,
                imageVector = ImageVector.vectorResource(id = i),
                contentDescription = ""
            )

        }
    }
}

fun randomFloatInRange(min: Float, max: Float): Float {
    return Random.nextFloat() * (max - min) + min
}

private fun calculateDistance(centroid: Offset): Float {
    return sqrt(centroid.x.pow(2) + centroid.y.pow(2))
}

@Composable
fun ColumnScope.Bottom(vm: MainViewModel) {

//    var currentProgress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    var isPlay by remember {
        mutableStateOf(false)
    }

//    LaunchedEffect(key1 = Unit) {
//        while (true) {
//            currentProgress += 1f / 100
//            delay(100)
//            if (currentProgress >= 10f) {
//                currentProgress = 0f
//            }
//        }
//    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(2f)
    ) {

        val isEnd by vm.endTrack.collectAsState()
        var currentProgress by remember {
            mutableFloatStateOf(0f)
        }

//        LaunchedEffect(key1 = isEnd) {
//            if (isEnd) {
//                isPlay = false
//            }
//        }
        val duration = vm.getDurationAudio()

        LinearProgressIndicator(
            progress = currentProgress / (duration ?: 0),
            modifier = Modifier.fillMaxWidth(),
        )

        LaunchedEffect(key1 = isPlay) {
            while (isPlay) {
                currentProgress = (vm.getCurrentPositionAudio() ?: 0).toFloat()
                delay(10)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${vm.getCurrentPositionAudio() ?: 0}",
                color = Color.White,
                fontFamily = interFontFamily,
                modifier = Modifier.weight(2f)
            )
            Row(modifier = Modifier.weight(8f), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.goAudioTrackTo(0) }) {
                    Icon(ImageVector.vectorResource(R.drawable.group_back), "", tint = Color.Green)
                }
                IconButton(onClick = {
                    isPlay = if (isPlay) {
                        vm.pauseAudioTrack()
                        !isPlay
                    } else {
                        vm.playAudioTrack()
                        !isPlay
                    }
                }) {
                    Icon(
                        if (isPlay) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "",
                        tint = Color.Green
                    )
                }
                IconButton(onClick = { vm.goToEnd() }) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.group_forward),
                        "",
                        tint = Color.Green
                    )
                }
            }
            Text(
                "${duration}",
                color = Color.White,
                fontFamily = interFontFamily,
                modifier = Modifier.weight(2f)
            )
        }

    }
}

suspend fun PointerInputScope.detectTransformGesturesAndEnd(
    panZoomLock: Boolean = false,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit,
    onGestureEnd: () -> Unit
) {
//    awaitEachGesture {\
    forEachGesture {
        awaitPointerEventScope {
            var rotation = 0f
            var zoom = 1f
            var pan = Offset.Zero
            var pastTouchSlop = false
            val touchSlop = viewConfiguration.touchSlop
            var lockedToPanZoom = false

            awaitFirstDown(requireUnconsumed = false)
            do {
                val event = awaitPointerEvent()
                val canceled = event.changes.any { it.isConsumed }
                if (!canceled) {
                    val zoomChange = event.calculateZoom()
                    val rotationChange = event.calculateRotation()
                    val panChange = event.calculatePan()

                    if (!pastTouchSlop) {
                        zoom *= zoomChange
                        rotation += rotationChange
                        pan += panChange

                        val centroidSize = event.calculateCentroidSize(useCurrent = false)
                        val zoomMotion = abs(1 - zoom) * centroidSize
                        val rotationMotion =
                            abs(rotation * kotlin.math.PI.toFloat() * centroidSize / 180f)
                        val panMotion = pan.getDistance()

                        if (zoomMotion > touchSlop ||
                            rotationMotion > touchSlop ||
                            panMotion > touchSlop
                        ) {
                            pastTouchSlop = true
                            lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                        }
                    }

                    if (pastTouchSlop) {
                        val centroid = event.calculateCentroid(useCurrent = false)
                        val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                        if (effectiveRotation != 0f ||
                            zoomChange != 1f ||
                            panChange != Offset.Zero
                        ) {
                            onGesture(centroid, panChange, zoomChange, effectiveRotation)
                        }
                        event.changes.forEach {
                            if (it.positionChanged()) {
                                it.consume()
                            }
                        }
                    }
                }
            } while (!canceled && event.changes.any { it.pressed })

            onGestureEnd()
        }
    }
//    }
}