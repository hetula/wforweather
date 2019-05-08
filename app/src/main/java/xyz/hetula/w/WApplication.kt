package xyz.hetula.w

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import xyz.hetula.w.di.backendModule
import xyz.hetula.w.di.schedulerModule
import xyz.hetula.w.di.uiModule

class WApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@WApplication)
            androidFileProperties()

            modules(schedulerModule, backendModule, uiModule)
        }
    }
}