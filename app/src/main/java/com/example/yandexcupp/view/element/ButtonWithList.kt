package com.example.yandexcupp.view.element

import android.content.res.Configuration
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yandexcupp.R
import com.example.yandexcupp.data.Sample
import com.example.yandexcupp.data.SampleEnum
import com.example.yandexcupp.data.dataStore
import com.example.yandexcupp.util.ysFontFamily
import kotlinx.coroutines.delay


//val guitarLabels = listOf(GuitarSample.guitar1, GuitarSample.guitar2, GuitarSample.guitar3)
//val drumLabels = listOf(DrumSample.drum1, DrumSample.drum2, DrumSample.drum3)
//val fluteLabels = listOf(FluteSample.flute1, FluteSample.flute2, FluteSample.flute3)



@Composable
fun RowScope.ButtonWithList(
    icon: Int,
    labels: List<Sample>,
    onInput: (Sample) -> Unit,
    onEnd: (Sample?) -> Unit
) {

//    val isOpen

    var isExpanded by remember { mutableStateOf(false) }
    val transition = updateTransition(isExpanded, label = "button state")
    var isLongPressActive by remember { mutableStateOf(false) }

    val expandColor by transition.animateColor(label = "expand color") {
        if (it) Color(0xFFA7DA10) else Color.White
    }

    val gradient = Brush.verticalGradient(
        0.0f to Color.Transparent, 0.5f to Color.White, 1f to Color.Transparent
    )


    var selectedItemIndex by remember { mutableStateOf<Int?>(null) }

    var showAnimate by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = showAnimate){
        if (showAnimate){
            isExpanded = true
            delay(500)
            isExpanded = false
            showAnimate = false
        }
    }



    BoxWithConstraints(
        modifier = Modifier
            .padding(16.dp)
            .weight(1f)
            .background(expandColor, RoundedCornerShape(50.dp))
            .animateContentSize()
            .pointerInput(Unit) {


                detectTapGestures(
                    onLongPress = {
                        isExpanded = !isExpanded

                    },
                    onTap = {
                        showAnimate = true
                        onEnd(labels.first())
                    }
                )


            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(onDragEnd = {

                    if (selectedItemIndex != null) {
                        onEnd(labels[selectedItemIndex!!])
                    } else {
                        onEnd(null)
                    }
                    selectedItemIndex = null
                    isExpanded = !isExpanded


                }) { change, dragAmount ->

                    val positionY = change.position.y

                    when {
                        positionY <= size.width
                            .toDp()
                            .toPx() -> {
                            selectedItemIndex = null
                        }

                        positionY in size.width
                            .toDp()
                            .toPx()..(size.width.toDp() + 26.dp * labels.size).toPx() -> {
                            selectedItemIndex = ((positionY - (size.width)
                                .toDp()
                                .toPx()) / 26.dp.toPx()).toInt()
                            onInput(labels[selectedItemIndex!!])
                        }

                        else -> {
                            selectedItemIndex = null
                        }
                    }
                    change.consume()
                }
            }, contentAlignment = Alignment.Center
    ) {

        val width = maxWidth

        Column(
            modifier = Modifier
                .sizeIn(minWidth = width, minHeight = width)
                .padding(bottom = if (isExpanded) 36.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                ImageVector.vectorResource(id = icon),
                contentDescription = "",
                modifier = Modifier.padding(top = 28.dp).sizeIn(minWidth = 44.dp)
            )
            if (isExpanded) {

                labels.forEachIndexed { idx, sample ->
                    Text(
                        text = sample.id,
                        fontSize = 14.sp,
                        fontFamily = ysFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(if (selectedItemIndex == idx) gradient else SolidColor(Color.Transparent))
                            .padding(horizontal = 4.dp, vertical = 12.dp)
                            .width(width)
                            .height(18.dp),
                        maxLines = 1,
                        color = Color.Black
                    )
                }
            }
        }
    }

}