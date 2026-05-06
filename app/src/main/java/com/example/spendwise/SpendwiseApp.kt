package com.example.spendwise

import android.app.Application

class SpendwiseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppData.init(this)
    }
}
