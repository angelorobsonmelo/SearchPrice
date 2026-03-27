package com.example.searchprice

import android.app.Application
import com.example.searchprice.di.appModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

/**
 * Custom Application class that bootstraps Koin before any component runs.
 *
 * This is required for Android Auto support: the [auto.SearchPriceCarAppService]
 * is a bound Service that may start independently of [MainActivity] (e.g. when
 * the user opens the app from the car's head unit without having launched the
 * phone UI first). Koin must be available before the Car App screens request
 * their injected dependencies.
 *
 * The [GlobalContext.getOrNull] guard makes the call idempotent so that
 * instrumented tests that start Koin themselves do not trigger a double-init.
 */
class SearchPriceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                modules(appModule)
            }
        }
    }
}
