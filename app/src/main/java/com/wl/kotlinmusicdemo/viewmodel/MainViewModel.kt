package com.wl.kotlinmusicdemo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData as MutableLiveData1

class MainViewModel : ViewModel() {


    open val testData= MutableLiveData1<String>()


    open val position= MutableLiveData1<Int>()

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