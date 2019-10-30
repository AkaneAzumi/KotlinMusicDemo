package com.wl.kotlinmusicdemo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.wl.kotlinmusicdemo.musicmodel.Music

class MainViewModel : ViewModel() {


    private val testData= MutableLiveData<String>()


    private val position= MutableLiveData<Int>()

    val currentMusic=MutableLiveData<Music>()

    fun postCurrentMusic(music: Music){
        currentMusic.postValue(music)
    }

    fun postpositonValue(value:Int){
        position.postValue(value)
    }

    fun posttestData(value: String){
        testData.postValue(value)
    }
    val _testData:LiveData<String>
    get() = testData
    companion object{
        fun newInstance()=MainViewModel()
    }

}