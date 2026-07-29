package app.grapheneos.deskclock.core.di

import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.presentation.AlarmViewModel
import app.grapheneos.deskclock.alarm.presentation.popup.AlarmPopUpViewModel
import app.grapheneos.deskclock.alarm.service.AlarmController
import app.grapheneos.deskclock.alarm.service.AlarmNotificationManager
import app.grapheneos.deskclock.clock.data.ClockRepository
import app.grapheneos.deskclock.clock.presentation.ClockViewModel
import app.grapheneos.deskclock.core.audio.AudioPlayer
import app.grapheneos.deskclock.core.audio.VibrationManager
import app.grapheneos.deskclock.core.database.AppDatabase
import app.grapheneos.deskclock.core.database.SettingsDataStore
import app.grapheneos.deskclock.core.database.getDatabaseBuilder
import app.grapheneos.deskclock.core.database.getRoomDatabase
import app.grapheneos.deskclock.core.notification.ChannelManager
import app.grapheneos.deskclock.settings.data.SettingsRepository
import app.grapheneos.deskclock.settings.presentation.SettingsViewModel
import app.grapheneos.deskclock.stopwatch.data.StopwatchRepository
import app.grapheneos.deskclock.stopwatch.presentation.StopwatchViewModel
import app.grapheneos.deskclock.stopwatch.service.StopwatchController
import app.grapheneos.deskclock.stopwatch.service.StopwatchNotificationManager
import app.grapheneos.deskclock.timer.data.TimerRepository
import app.grapheneos.deskclock.timer.presentation.TimerViewModel
import app.grapheneos.deskclock.timer.presentation.popup.TimerPopUpViewModel
import app.grapheneos.deskclock.timer.service.TimerController
import app.grapheneos.deskclock.timer.service.TimerNotificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreModule = module {
    single {
        getDatabaseBuilder(context = get())
    }

    single<AppDatabase> {
        getRoomDatabase(builder = get())
    }

    single(named("AttributedContext")) {
        androidContext().createAttributionContext("deskclock_service")
    }

    single { ChannelManager(get()) }
    single { AudioPlayer(get()) }
    single { VibrationManager(get()) }
}

val settingsModule = module {
    single { SettingsDataStore(androidContext()) }
    single { SettingsRepository(get()) }

    viewModelOf(::SettingsViewModel)
}

val alarmModule = module {
    single {
        get<AppDatabase>().alarmDao()
    }

    single { AlarmController(get(named("AttributedContext"))) }

    single {
        AlarmRepository(
            get(),
            get()
        )
    }

    single {
        AlarmNotificationManager(
            get(named("AttributedContext"))
        )
    }

    viewModelOf(::AlarmViewModel)
    viewModelOf(::AlarmPopUpViewModel)
}

val clockModule = module {
    single {
        get<AppDatabase>().clockDao()
    }

    single {
        ClockRepository(get())
    }

    viewModelOf(::ClockViewModel)
}

val timerModule = module {
    single { TimerController(get(named("AttributedContext"))) }
    single { TimerNotificationManager(get(named("AttributedContext"))) }

    single {
        TimerRepository(get<TimerController>())
    }

    viewModelOf(::TimerViewModel)
    viewModelOf(::TimerPopUpViewModel)
}

val stopwatchModule = module {
    single { StopwatchController(get()) }
    single { StopwatchNotificationManager(get()) }
    single { StopwatchRepository(get()) }

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
