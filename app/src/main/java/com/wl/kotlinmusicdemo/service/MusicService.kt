package com.wl.kotlinmusicdemo.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.wl.kotlinmusicdemo.MainActivity
import com.wl.kotlinmusicdemo.musicmodel.Music
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class MusicService :Service(){
    private var mediaPlayer:MediaPlayer=MediaPlayer()

    private var currentTime:Long?=null
    private var currentMusic:Music?=null
    enum class List_Ctrl_Code{
        LIST_LOOP,
        SINGLE_LOOP,
        LIST_ALL_LOOP
    }
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }
    //暂停后播放
    fun onPlay(){

    }
    //播放音乐的共用方法
    fun onStart(){
        mediaPlayer.reset()
        mediaPlayer.setDataSource(currentMusic?.music_url)
        mediaPlayer.prepare()
        mediaPlayer.start()

    }
    //下一曲
    fun onNext(){

    }
    //上一曲
    fun onPervoius(){}
    //暂停
    fun onPause(){}

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @Subscribe
    private fun getCurrentMusic(music: Music){
        currentMusic=music
    }

    @Subscribe
    private fun  getCtrlType(ctrlplay_ctrl_type: MainActivity.play_ctrl_type){
        when(ctrlplay_ctrl_type){
            MainActivity.play_ctrl_type.NEXT-> onNext()
            MainActivity.play_ctrl_type.PREVIOUS->onPervoius()
            MainActivity.play_ctrl_type.PAUSE->onPause()
            MainActivity.play_ctrl_type.PLAY->onPlay()
        }
    }


}