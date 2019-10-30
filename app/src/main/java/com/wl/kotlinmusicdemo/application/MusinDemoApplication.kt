package com.wl.kotlinmusicdemo.application

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tencent.bugly.Bugly

class MusinDemoApplication :Application(){

    override fun onCreate() {
        super.onCreate()
        Bugly.init(applicationContext,"a4cf7bcda9",true)
    }


}