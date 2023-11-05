package com.example.yandexcupp.data

import com.example.yandexcupp.R
import java.util.concurrent.atomic.AtomicInteger

enum class SampleEnum{
    Guitar, Drum, Flute, Mic
}

object Counter{
    private var allCounter: AtomicInteger = AtomicInteger(0)

    fun getId() = allCounter.getAndAdd(1)
}

data class Sample(
    val id: String,
    val sampleEnum: SampleEnum,
    val path: Int,
)

interface Layer{
    val sampleEnum: SampleEnum
    val id: String
    var volume: Float
    var rate: Float

}

data class SampleLayer(
    override val sampleEnum: SampleEnum,
    val path: Int,
): Layer{
    override val id = sampleEnum.name + Counter.getId()
    override var volume: Float = 0.5f
    override var rate: Float = 1f

}

data class MicLayer(
    override val sampleEnum: SampleEnum,
    var filePath: String = ""
): Layer{
    override val id = sampleEnum.name + Counter.getId()
    override var volume: Float = 0.5f
    override var rate: Float = 1f

}



val dataStore = mutableListOf(
    Sample("Гитара 1", SampleEnum.Guitar, R.raw.guitar_1),
    Sample("Гитара 2", SampleEnum.Guitar, R.raw.guitar_2),
    Sample("Гитара 3", SampleEnum.Guitar, R.raw.guitar_3),
    Sample("Барабаны 1",SampleEnum.Drum, R.raw.drum_1),
    Sample("Барабаны 2",SampleEnum.Drum, R.raw.drum_2),
    Sample("Барабаны 3",SampleEnum.Drum, R.raw.drum_3),
    Sample("Духовые 1",SampleEnum.Flute, R.raw.flute_1),
    Sample("Духовые 2",SampleEnum.Flute, R.raw.flute_2),
    Sample("Духовые 3",SampleEnum.Flute, R.raw.flute_3)
)


object FinalTrack{
    var path: String = ""
}

//object GuitarSample{
//    val guitar1 = "guitar1" to R.raw.guitar_1
//    val guitar2 = "guitar2" to R.raw.guitar_2
//    val guitar3 = "guitar3" to R.raw.guitar_3
//}
//
//object DrumSample{
//    val drum1 = "drum1" to R.raw.drum_1
//    val drum2 = "drum2" to R.raw.drum_2
//    val drum3 = "drum3" to R.raw.drum_3
//}
//
//object FluteSample{
//    val flute1 = "flute1" to R.raw.flute_1
//    val flute2 = "flute2" to R.raw.flute_2
//    val flute3 = "flute3" to R.raw.flute_3
//}