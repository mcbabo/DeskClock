package app.grapheneos.deskclock.core.di

import app.grapheneos.deskclock.core.database.AppDatabase
import app.grapheneos.deskclock.core.database.getDatabaseBuilder
import app.grapheneos.deskclock.core.database.getRoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
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


fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)
        modules(
            targetModule
        )
    }
}
