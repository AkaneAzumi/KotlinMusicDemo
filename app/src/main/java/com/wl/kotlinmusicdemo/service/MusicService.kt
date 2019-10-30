package com.wl.kotlinmusicdemo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wl.kotlinmusicdemo.databean.*
import com.wl.kotlinmusicdemo.musicmodel.Music
import com.wl.kotlinmusicdemo.musicmodel.getMusicListFromPhone
import com.wl.kotlinmusicdemo.utils.List_Ctrl_Code
import com.wl.kotlinmusicdemo.databean.ProgressData
import com.wl.kotlinmusicdemo.utils.play_ctrl_type
import com.wl.kotlinmusicdemo.utils.timeFormat
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*


class MusicService : Service() {
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var list: MutableList<Music>? = null
    private var currentTime: Long? = null
    private var currentMusic: Music? = null
    private var currentPostion: Int = 0
    private var timer: Timer? = null
    private var postProgress: TimerTask? = null
    private var postStatus: TimerTask? = null


    private var ctrlCode = List_Ctrl_Code.LIST_ALL_LOOP

    override fun onCreate() {
        super.onCreate()
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
            mediaPlayer.reset()
            mediaPlayer.setDataSource(currentMusic?.music_url)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                when (ctrlCode) {
                    List_Ctrl_Code.SINGLE_LOOP -> mediaPlayer.isLooping = true
                    List_Ctrl_Code.LIST_LOOP, List_Ctrl_Code.LIST_ALL_LOOP -> onNext()
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
                List_Ctrl_Code.LIST_LOOP ->
                    if (currentPostion == list!!.size) {
                        mediaPlayer.stop()
                    }
                List_Ctrl_Code.LIST_ALL_LOOP, List_Ctrl_Code.SINGLE_LOOP -> {
                    if (currentPostion == list!!.size) {
                        currentPostion = 0
                    }
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
                List_Ctrl_Code.LIST_LOOP ->
                    if (currentPostion < 0) {
                        mediaPlayer.stop()
                    }
                List_Ctrl_Code.LIST_ALL_LOOP, List_Ctrl_Code.SINGLE_LOOP -> {
                    if (currentPostion < 0) {
                        currentPostion = list!!.size - 1
                    }
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        releaseTimer()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    @Subscribe
    fun getCtrlType(ctrlplay_ctrl_type: play_ctrl_type) {
        when (ctrlplay_ctrl_type) {
            play_ctrl_type.NEXT -> onNext()
            play_ctrl_type.PREVIOUS -> onPervoius()
            play_ctrl_type.PAUSE -> onPause()
            play_ctrl_type.PLAY -> onPlay()
            play_ctrl_type.START -> onStart()
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