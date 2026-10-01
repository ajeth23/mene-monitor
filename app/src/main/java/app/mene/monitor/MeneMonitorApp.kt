package app.mene.monitor

import android.app.Application
import app.mene.monitor.core.di.AppContainer
import app.mene.monitor.core.di.DefaultAppContainer

class MeneMonitorApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
