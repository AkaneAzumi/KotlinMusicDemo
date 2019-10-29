package com.wl.kotlinmusicdemo.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.wl.kotlinmusicdemo.MainActivity
import com.wl.kotlinmusicdemo.musicmodel.Music
import com.wl.kotlinmusicdemo.musicmodel.getMusicListFromPhone
import com.wl.kotlinmusicdemo.utils.ProgressData
import com.wl.kotlinmusicdemo.utils.timeFormat
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*


class MusicService :Service(){
    private var mediaPlayer:MediaPlayer=MediaPlayer()
    private var list= getMusicListFromPhone(this)
    private var currentTime:Long?=null
    private var currentMusic:Music?=null
    private var currentPostion:Int=0
    private var timer:Timer?=null
    private var postProgress:TimerTask?=null
    enum class List_Ctrl_Code{
        LIST_LOOP,
        SINGLE_LOOP,
        LIST_ALL_LOOP
    }
    private var ctrlCode=List_Ctrl_Code.LIST_ALL_LOOP
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        initTimer()
    }

    fun initTimer(){
        timer= Timer("postProgress")
        postProgress=object:TimerTask(){
            override fun run() {
                var progress=ProgressData((mediaPlayer.currentPosition as Float)/(mediaPlayer.duration as Float))

                var current_time_string= timeFormat(mediaPlayer.currentPosition)
                EventBus.getDefault().post(progress)
                EventBus.getDefault().post(current_time_string)
            }


        }
        timer?.schedule(postProgress,0,1000)

    }

    fun releaseTimer(){
        postProgress?.cancel()
        postProgress=null
        timer?.cancel()
        timer=null
    }




    //暂停后播放
    fun onPlay(){
        if (!mediaPlayer.isPlaying){
            mediaPlayer.start()
        }
    }
    //播放音乐的共用方法
    fun onStart(){
        mediaPlayer.reset()
        mediaPlayer.setDataSource(currentMusic?.music_url)
        mediaPlayer.prepare()
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener{
            when(ctrlCode){
                List_Ctrl_Code.SINGLE_LOOP->mediaPlayer.isLooping=true
                List_Ctrl_Code.LIST_LOOP,List_Ctrl_Code.LIST_ALL_LOOP->onNext()
            }
        }
    }
    //下一曲
    fun onNext(){
       if (mediaPlayer.isPlaying){
           currentPostion++
           when(ctrlCode){
               List_Ctrl_Code.LIST_LOOP->
                   if (currentPostion===list.size){
                       mediaPlayer.stop()
                   }
               List_Ctrl_Code.LIST_ALL_LOOP,List_Ctrl_Code.SINGLE_LOOP->{
                   if (currentPostion===list.size){
                       currentPostion=0
                   }
               }
           }
           currentMusic=list[currentPostion]
           EventBus.getDefault().post(currentPostion)
           EventBus.getDefault().post(currentMusic)
           onStart()
       }
    }

    //上一曲
    fun onPervoius(){
        if (mediaPlayer.isPlaying){
            currentPostion--
            when(ctrlCode){
                List_Ctrl_Code.LIST_LOOP->
                    if (currentPostion<0){
                        mediaPlayer.stop()
                    }
                List_Ctrl_Code.LIST_ALL_LOOP,List_Ctrl_Code.SINGLE_LOOP->{
                    if (currentPostion<0){
                        currentPostion=list.size-1
                    }
                }

            }
            currentMusic=list[currentPostion]
            EventBus.getDefault().post(currentPostion)
            EventBus.getDefault().post(currentMusic)
            onStart()

        }
    }
    //暂停
    fun onPause(){
        if (mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        releaseTimer()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    @Subscribe
    private fun  getCtrlType(ctrlplay_ctrl_type: MainActivity.play_ctrl_type){
        when(ctrlplay_ctrl_type){
            MainActivity.play_ctrl_type.NEXT-> onNext()
            MainActivity.play_ctrl_type.PREVIOUS->onPervoius()
            MainActivity.play_ctrl_type.PAUSE->onPause()
            MainActivity.play_ctrl_type.PLAY->onPlay()
            MainActivity.play_ctrl_type.START->onStart()
        }
    }


}