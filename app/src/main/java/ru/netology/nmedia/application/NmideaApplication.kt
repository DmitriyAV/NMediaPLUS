package ru.netology.nmedia.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NmideaApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}