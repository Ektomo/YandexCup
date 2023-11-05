package com.example.lct2023.di

import android.content.Context
import com.example.yandexcupp.player.MultiTrackPlayer
import com.example.yandexcupp.player.Recorder
import com.example.yandexcupp.player.TrackPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


//    @Singleton
//    @Provides
//    fun provideVoiceRecognitionApp(
//        @ApplicationContext app: Context
//    ): VoiceRecognizer = VoiceRecognizer(app)

    @Singleton
    @Provides
    fun provideTrackPlayer(
        @ApplicationContext app: Context
    ): TrackPlayer = TrackPlayer(app, 30)


    @Singleton
    @Provides
    fun provideMultiTrackPlayer(
        @ApplicationContext app: Context
    ): MultiTrackPlayer = MultiTrackPlayer(app)

    @Singleton
    @Provides
    fun provideRecorder(
        @ApplicationContext app: Context
    ): Recorder = Recorder(app)

}