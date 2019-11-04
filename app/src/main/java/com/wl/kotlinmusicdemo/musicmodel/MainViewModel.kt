package com.wl.kotlinmusicdemo.musicmodel

import android.net.Uri
import android.view.ViewManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.net.URL

class MainViewModel :ViewModel(){

    private var _imageUrl= MutableLiveData<Uri>()
    fun postimageUrl(image: Uri){
        _imageUrl.postValue(image)
    }
    val imageUri:LiveData<Uri>
        get() = _imageUrl

}