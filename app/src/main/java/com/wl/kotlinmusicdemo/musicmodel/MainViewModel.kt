package com.wl.kotlinmusicdemo.musicmodel

import android.net.Uri
import android.view.ViewManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.net.URL

class MainViewModel :ViewModel(){

    private var _imageUrl= MutableLiveData<Uri>()

    private var _musicList=MutableLiveData<MutableList<Music>>()
    fun postimageUrl(image: Uri){
        _imageUrl.postValue(image)
    }

    fun postMusic(list: MutableList<Music>)
    {
        _musicList.postValue(list)
    }

    val musicList:LiveData<MutableList<Music>>
    get() = _musicList
    val imageUri:LiveData<Uri>
        get() = _imageUrl

}