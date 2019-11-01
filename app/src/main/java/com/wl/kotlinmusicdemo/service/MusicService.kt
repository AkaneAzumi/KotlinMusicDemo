package com.wl.kotlinmusicdemo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wl.kotlinmusicdemo.databean.*
import com.wl.kotlinmusicdemo.musicmodel.Music
import com.wl.kotlinmusicdemo.musicmodel.getMusicListFromPhone
import com.wl.kotlinmusicdemo.databean.ProgressData
import com.wl.kotlinmusicdemo.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt


class MusicService : Service() {
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var list: MutableList<Music>? = null
    private var currentMusic: Music? = null
    private var currentPostion: Int = 0
    private var timer: Timer? = null
    private var postProgress: TimerTask? = null
    private var postStatus: TimerTask? = null
    private var sharedPrefrenceUtils:SharedPrefrenceUtils=SharedPrefrenceUtils(this)


    private var ctrlCode = ListCtrlCode.LIST_ALL_LOOP

    override fun onCreate() {
        super.onCreate()
        sharedPrefrenceUtils.getData("CtrlCode",1)?.let {
            ctrlCode=it as Int
            Log.d("CODE SI","$ctrlCode")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var manager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(
                    "40",
                    "MYMusicService",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            var builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "40")
            startForeground(2, builder.build())
        }
        EventBus.getDefault().register(this)
        list = getMusicListFromPhone(this)
        initTimer()

    }

    fun initTimer() {
        timer = Timer("postProgress")
        postProgress = object : TimerTask() {
            override fun run() {
                try {
                    if (mediaPlayer.isPlaying) {
                        var progress =
                            ProgressData(100 * (mediaPlayer.currentPosition.toFloat()) / (mediaPlayer.duration.toFloat()))
                        var progressToFragment=ProgressToFragment((100 * (mediaPlayer.currentPosition.toFloat()) / (mediaPlayer.duration.toFloat())).toInt())
                        var current_time_string = timeFormat(mediaPlayer.currentPosition)
                        EventBus.getDefault().post(progress)
                        EventBus.getDefault().post(progressToFragment)
                        EventBus.getDefault().post(ServiceToFragment(current_time_string))
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }

            }
        }
        postStatus = object : TimerTask() {
            override fun run() {
                try {
                    EventBus.getDefault().post(MediaPlayerStatus(mediaPlayer.isPlaying))
                    EventBus.getDefault().post(MediaPlayerStatusToFragment(mediaPlayer.isPlaying))

                }catch (e:java.lang.IllegalStateException){

                }

            }

        }
        timer?.schedule(postStatus, 0, 100)
        timer?.schedule(postProgress, 0, 1000)

    }

    fun releaseTimer() {
        postStatus?.cancel()
        postStatus = null
        postProgress?.cancel()
        postProgress = null
        timer?.cancel()
        timer = null
    }

    //暂停后播放
    fun onPlay() {
        try {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    //播放音乐的共用方法
    fun onStart() {
        try {
            currentMusic = list?.get(currentPostion)
            EventBus.getDefault().post(ServiceToMain(currentPostion))
            EventBus.getDefault().postSticky(currentMusic)
            sharedPrefrenceUtils.SaveData("Music",currentMusic!!)
            mediaPlayer.reset()
            mediaPlayer.setDataSource(currentMusic?.music_url)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                when (ctrlCode) {
                    ListCtrlCode.SINGLE_LOOP ->{
                        it.start()
                        it.isLooping = true
                    }
                    ListCtrlCode.LIST_ALL_LOOP ,ListCtrlCode.RONDOM-> onNext()

                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    //下一曲
    fun onNext() {
        try {
            currentPostion++
            when (ctrlCode) {
                ListCtrlCode.LIST_ALL_LOOP, ListCtrlCode.SINGLE_LOOP -> {
                    if (currentPostion == list!!.size) {
                        currentPostion = 0
                    }
                }
                ListCtrlCode.RONDOM->{
                    var r= Random
                    currentPostion=r.nextInt(list!!.size)
                }
            }
            onStart()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    //上一曲
    fun onPervoius() {
        try {


            currentPostion--
            when (ctrlCode) {
                ListCtrlCode.LIST_ALL_LOOP, ListCtrlCode.SINGLE_LOOP -> {
                    if (currentPostion < 0) {
                        currentPostion = list!!.size - 1
                    }
                }
                ListCtrlCode.RONDOM->{
                    var r= Random
                    currentPostion=r.nextInt(list!!.size)
                }
            }
            onStart()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    //暂停
    fun onPause() {
        try {


            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun releaseMedia(){
        mediaPlayer.stop()
        mediaPlayer?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        releaseMedia()
        releaseTimer()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @Subscribe
    fun getListType(Code: ListCtrlCode){
        ctrlCode=Code.listCtrlCode
    }


    @Subscribe
    fun getCtrlType(ctrlType: playCtrlType) {
        when (ctrlType.playCtrlType) {
            playCtrlType.NEXT -> onNext()
            playCtrlType.PREVIOUS -> onPervoius()
            playCtrlType.PAUSE -> onPause()
            playCtrlType.PLAY -> onPlay()
            playCtrlType.START -> onStart()
        }
    }

    @Subscribe
    fun getCurrentFromMain(mainToService: MainToService) {
        currentPostion = mainToService.current
    }

    @Subscribe
    fun getChangeProgress(change: ProgressChange) {
        mediaPlayer.seekTo(change.change * mediaPlayer.duration / 100)
    }


}