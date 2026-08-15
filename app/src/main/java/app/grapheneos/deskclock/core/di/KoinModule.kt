package app.grapheneos.deskclock.core.di

import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.presentation.AlarmViewModel
import app.grapheneos.deskclock.alarm.presentation.popup.AlarmPopUpViewModel
import app.grapheneos.deskclock.alarm.service.AlarmController
import app.grapheneos.deskclock.clock.data.ClockRepository
import app.grapheneos.deskclock.clock.presentation.ClockViewModel
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.core.audio.VibrationManager
import app.grapheneos.deskclock.core.database.AppDatabase
import app.grapheneos.deskclock.core.database.SettingsDataStore
import app.grapheneos.deskclock.core.database.getDatabaseBuilder
import app.grapheneos.deskclock.core.database.getRoomDatabase
import app.grapheneos.deskclock.core.notification.ChannelManager
import app.grapheneos.deskclock.core.presentation.MainActivityViewModel
import app.grapheneos.deskclock.core.ringtone.RingtoneRepository
import app.grapheneos.deskclock.core.util.Constants
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.settings.presentation.SettingsViewModel
import app.grapheneos.deskclock.stopwatch.data.StopwatchRepository
import app.grapheneos.deskclock.stopwatch.presentation.StopwatchViewModel
import app.grapheneos.deskclock.timer.data.TimerRepository
import app.grapheneos.deskclock.timer.presentation.TimerViewModel
import app.grapheneos.deskclock.timer.presentation.popup.TimerPopUpViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreModule = module {
    single { getDatabaseBuilder(get()) }
    single { getRoomDatabase(get()) }

    single(named("AttributedContext")) {
        androidContext().createAttributionContext(Constants.ATTRIBUTION_TAG_DESKCLOCK_SERVICE)
    }

    singleOf(::ChannelManager)
    singleOf(::AudioPlayer)
    singleOf(::VibrationManager)
    singleOf(::RingtoneRepository)

    viewModelOf(::MainActivityViewModel)
}

val settingsModule = module {
    singleOf(::SettingsDataStore)
    singleOf(::SettingsRepository)

    viewModelOf(::SettingsViewModel)
}

val alarmModule = module {
    single { get<AppDatabase>().alarmDao() }
    single { AlarmController(get(named("AttributedContext"))) }
    singleOf(::AlarmRepository)

    viewModelOf(::AlarmViewModel)
    viewModelOf(::AlarmPopUpViewModel)
}

val clockModule = module {
    single { get<AppDatabase>().clockDao() }
    singleOf(::ClockRepository)

    viewModelOf(::ClockViewModel)
}

val timerModule = module {
    singleOf(::TimerRepository)

    viewModelOf(::TimerViewModel)
    viewModelOf(::TimerPopUpViewModel)
}

val stopwatchModule = module {
    singleOf(::StopwatchRepository)

    viewModelOf(::StopwatchViewModel)
}

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)
        modules(
            coreModule,
            settingsModule,
            alarmModule,
            clockModule,
            timerModule,
            stopwatchModule
        )
    }
}
