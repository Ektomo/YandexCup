package com.example.yandexcupp.view.block

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.example.yandexcupp.R
import com.example.yandexcupp.data.Sample
import com.example.yandexcupp.data.SampleEnum
import com.example.yandexcupp.data.dataStore
import com.example.yandexcupp.view.element.ButtonWithList

val guitarLabels = dataStore.filter { it.sampleEnum == SampleEnum.Guitar }
val drumLabels = dataStore.filter { it.sampleEnum == SampleEnum.Drum }
val fluteLabels = dataStore.filter { it.sampleEnum == SampleEnum.Flute }

@Composable
fun RowButtons(
    modifier: Modifier,
    onEnd: (Sample?) -> Unit,
    onInput: (Sample) -> Unit
) {

    val configuration = LocalConfiguration.current
    val needSpacer by remember(configuration.orientation) {
        mutableStateOf(
            when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    true
                }

                else -> {
                    false
                }
            }
        )
    }

    Row(modifier = modifier) {
        ButtonWithList(
            icon = R.drawable.guitar,
            labels = guitarLabels,
            onInput = onInput,
            onEnd = onEnd
        )
        if (needSpacer) {
            Spacer(modifier = Modifier.weight(1.5f))
        }
        ButtonWithList(
            icon = R.drawable.drums,
            labels = drumLabels,
            onInput = onInput,
            onEnd = onEnd
        )
        if (needSpacer) {
            Spacer(modifier = Modifier.weight(1.5f))
        }
        ButtonWithList(
            icon = R.drawable.duh,
            labels = fluteLabels,
            onInput = onInput,
            onEnd = onEnd
        )
    }

}