package app.grapheneos.deskclock.core.di

import app.grapheneos.deskclock.alarm.data.AlarmRepository
import app.grapheneos.deskclock.alarm.presentation.AlarmViewModel
import app.grapheneos.deskclock.alarm.presentation.popup.AlarmPopUpViewModel
import app.grapheneos.deskclock.alarm.service.AlarmController
import app.grapheneos.deskclock.alarm.service.AlarmNotificationManager
import app.grapheneos.deskclock.clock.data.ClockRepository
import app.grapheneos.deskclock.clock.presentation.ClockViewModel
import app.grapheneos.deskclock.core.database.AppDatabase
import app.grapheneos.deskclock.core.database.getDatabaseBuilder
import app.grapheneos.deskclock.core.database.getRoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val targetModule = module {
    single {
        getDatabaseBuilder(context = get())
    }

    single<AppDatabase> {
        getRoomDatabase(builder = get())
    }

    single(named("AttributedContext")) {
        androidContext().createAttributionContext("deskclock_service")
    }
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

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)
        modules(
            targetModule,
            alarmModule,
            clockModule
        )
    }
}
