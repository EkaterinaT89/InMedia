package ru.netology.inmedia.application

import android.app.Application
import ru.netology.inmedia.auth.AppAuth

class InMediaApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppAuth.initApp(this)
    }
}