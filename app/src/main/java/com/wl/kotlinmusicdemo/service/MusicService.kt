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
import com.wl.kotlinmusicdemo.MainActivity
import com.wl.kotlinmusicdemo.databean.MainToService
import com.wl.kotlinmusicdemo.databean.ServiceToMain
import com.wl.kotlinmusicdemo.musicmodel.Music
import com.wl.kotlinmusicdemo.musicmodel.getMusicListFromPhone
import com.wl.kotlinmusicdemo.utils.ProgressData
import com.wl.kotlinmusicdemo.utils.timeFormat
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*


class MusicService : Service() {
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var list: MutableList<Music>? = null
    private var currentTime: Long? = null
    private var currentMusic: Music? = null
    private var currentPostion: Int = 0
    private var timer: Timer? = null
    private var postProgress: TimerTask? = null

    enum class List_Ctrl_Code {
        LIST_LOOP,
        SINGLE_LOOP,
        LIST_ALL_LOOP
    }

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
                if (mediaPlayer.isPlaying) {
                    var progress =
                        ProgressData(100 * (mediaPlayer.currentPosition.toFloat()) / (mediaPlayer.duration.toFloat()))

                    var current_time_string = timeFormat(mediaPlayer.currentPosition)
                    EventBus.getDefault().post(progress)
                    EventBus.getDefault().post(current_time_string)
                }

            }


        }
        timer?.schedule(postProgress, 0, 1000)

    }

    fun releaseTimer() {
        postProgress?.cancel()
        postProgress = null
        timer?.cancel()
        timer = null
    }


    //暂停后播放
    fun onPlay() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    //播放音乐的共用方法
    fun onStart() {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(list?.get(currentPostion)?.music_url)
        mediaPlayer.prepare()
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            when (ctrlCode) {
                List_Ctrl_Code.SINGLE_LOOP -> mediaPlayer.isLooping = true
                List_Ctrl_Code.LIST_LOOP, List_Ctrl_Code.LIST_ALL_LOOP -> onNext()
            }
        }
    }

    //下一曲
    fun onNext() {
        currentPostion++
        when (ctrlCode) {
            List_Ctrl_Code.LIST_LOOP ->
                if (currentPostion === list!!.size) {
                    mediaPlayer.stop()
                }
            List_Ctrl_Code.LIST_ALL_LOOP, List_Ctrl_Code.SINGLE_LOOP -> {
                if (currentPostion === list!!.size) {
                    currentPostion = 0
                }
            }
        }
        currentMusic = list?.get(currentPostion)
        EventBus.getDefault().post(ServiceToMain(currentPostion))
        EventBus.getDefault().post(currentMusic)
        onStart()
    }

    //上一曲
    fun onPervoius() {
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
        currentMusic = list?.get(currentPostion)
        EventBus.getDefault().post(ServiceToMain(currentPostion))
        EventBus.getDefault().post(currentMusic)
        onStart()

    }

    //暂停
    fun onPause() {
        if (mediaPlayer.isPlaying) {
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
    fun getCtrlType(ctrlplay_ctrl_type: MainActivity.play_ctrl_type) {
        when (ctrlplay_ctrl_type) {
            MainActivity.play_ctrl_type.NEXT -> onNext()
            MainActivity.play_ctrl_type.PREVIOUS -> onPervoius()
            MainActivity.play_ctrl_type.PAUSE -> onPause()
            MainActivity.play_ctrl_type.PLAY -> onPlay()
            MainActivity.play_ctrl_type.START -> onStart()
        }
    }

    @Subscribe
    fun getCurrentFromMain(mainToService: MainToService) {
        currentPostion = mainToService.current
    }


}