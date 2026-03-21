package morse.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import morse.android.audio.IAudioPlayer
import morse.android.audio.MorseAudioPlayer
import morse.android.haptics.HapticsController
import morse.android.haptics.IHapticsController
import morse.android.persistence.IProgressRepository
import morse.android.persistence.ISettingsRepository
import morse.android.persistence.ProgressRepository
import morse.android.persistence.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingsModule {

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepository): ISettingsRepository

    @Binds @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepository): IProgressRepository

    @Binds @Singleton
    abstract fun bindAudioPlayer(impl: MorseAudioPlayer): IAudioPlayer

    @Binds @Singleton
    abstract fun bindHapticsController(impl: HapticsController): IHapticsController
}
