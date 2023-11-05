package com.example.yandexcupp.view.element

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yandexcupp.util.ysFontFamily
import kotlinx.coroutines.launch
import kotlin.math.pow



@Composable
fun CustomVerticalSlider(modifier: Modifier, value: Float, onValue: (Float) -> Unit) {

    var sliderValue by remember(value) { mutableFloatStateOf(1-value) }
    val steps = 30
    val coroutineScope = rememberCoroutineScope()
    val knobSize = 60.dp

    BoxWithConstraints(
        modifier = modifier
    ) {
        val height = this.maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..steps) {
                drawLine(
                    Color.White,
                    start = Offset(0f, size.height * i / (steps + 1)),
                    end = Offset(if (i % 5 == 0) 60f else 40f, size.height * i / (steps + 1)),
                    strokeWidth = 2f
                )
            }
        }

        Box(
            modifier = Modifier
                .height(knobSize)
                .align(Alignment.TopStart)
                .offset(y = ((height - knobSize) * (sliderValue)).coerceIn(0.dp, height-knobSize), x = (-24).dp)
                .pointerInput(Unit) {
                    detectDragGestures { _, offsetChange ->
                        coroutineScope.launch {
                            sliderValue += (offsetChange.y / (height-knobSize).toPx()).coerceIn(-1f, 1f)
                            sliderValue = sliderValue.coerceIn(0f, 1f)
                            onValue(1-sliderValue)
                        }
                    }
                }
                .rotate(-90f)
        ) {
            Text(
                text = "громкость",
                fontSize = 11.sp,
                fontFamily = ysFontFamily,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0xFFA7DA10), RoundedCornerShape(4.dp))
                    .padding(4.dp)
            )
        }

    }
}


@Composable
fun CustomHorizontalSlider(modifier: Modifier, value: Float, onValue: (Float) -> Unit) {

    var sliderValue by remember(value) { mutableFloatStateOf(value/3) }
    val steps = 30
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier
    ) {
        val width = this.maxWidth
        val knobSize = 70.dp

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 1..steps) {
                val position = i.toDouble() / steps
                val adjustedPosition = 1 - (1 - position).pow(1.5)
                val xPosition = size.width * adjustedPosition.toFloat()
                drawLine(
                    Color.White,
                    start = Offset(xPosition, size.height - 40f),
                    end = Offset(xPosition, size.height),
                    strokeWidth = 2f
                )
            }
        }

        Box(
            modifier = Modifier
                .width(knobSize)
                .align(Alignment.BottomStart)
                .offset(x = ((width - knobSize) * sliderValue).coerceIn(0.dp, width-knobSize)+8.dp)
                .pointerInput(Unit) {
                    detectDragGestures { _, offsetChange ->
                        coroutineScope.launch {
                            sliderValue += (offsetChange.x / (width-knobSize).toPx()).coerceIn(-1f, 1f)
                            sliderValue = sliderValue.coerceIn(0f, 1f)
                            onValue(sliderValue * 3f)
                        }
                    }
                }
        ) {
            Text(
                text = "скорость",
                fontSize = 11.sp,
                fontFamily = ysFontFamily,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0xFFA7DA10), RoundedCornerShape(4.dp))
                    .padding(4.dp)
            )
        }
    }
}