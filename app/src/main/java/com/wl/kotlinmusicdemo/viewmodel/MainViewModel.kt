package com.wl.kotlinmusicdemo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val position=MutableLiveData<Int>()

    fun postpositonValue(value:Int){
        position.postValue(value)
    }
}