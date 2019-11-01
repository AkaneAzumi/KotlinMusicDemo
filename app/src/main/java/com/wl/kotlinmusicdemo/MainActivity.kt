package com.wl.kotlinmusicdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.wl.kotlinmusicdemo.databean.MainToService
import com.wl.kotlinmusicdemo.databean.MediaPlayerStatus
import com.wl.kotlinmusicdemo.databean.ServiceToMain
import com.wl.kotlinmusicdemo.fragment.MusicPlayingDialog
import com.wl.kotlinmusicdemo.musicmodel.Music
import com.wl.kotlinmusicdemo.musicmodel.getMusicListFromPhone
import com.wl.kotlinmusicdemo.service.MusicService
import com.wl.kotlinmusicdemo.ui.MusicListAdapter
import com.wl.kotlinmusicdemo.databean.ProgressData
import com.wl.kotlinmusicdemo.utils.SharedPrefrenceUtils

import com.wl.kotlinmusicdemo.utils.playCtrlType

import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity(), View.OnClickListener {


    //按键点击事件监听
    override fun onClick(p0: View?) {
        when (p0?.id) {
            play_ctrl_next.id -> {
                play_ctrl_btn.isChecked=true
                EventBus.getDefault().post(playCtrlType(playCtrlType.NEXT))
            }
            ablum_image.id->{
                musicPlayingDialog.show(supportFragmentManager,"Dialog")
                EventBus.getDefault().postSticky(currentMusic)

            }
        }
    }

    private var musicPlayingDialog= MusicPlayingDialog()

    private var currentMusic: Music?=null
    private var timer: Timer? = null
    private var timer_imgrun: TimerTask? = null
    private var isCheck: Boolean = false
    private var adapter: MusicListAdapter? = null
    private var rotationS: Float = 0f
    private var music_lists:MutableList<Music>?=null
    var permissionList = mutableSetOf<String>()

    override fun onStart() {
        music_lists=getMusicListFromPhone(this)

        super.onStart()

        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }

    }

    override fun onResume() {
        var sharedPrefrenceUtils=SharedPrefrenceUtils(applicationContext)
       (sharedPrefrenceUtils.getData("Music",Music()))?.let {
           currentMusic=it as Music
           Log.d(TAG,"恢复界面${it.music_name}")
           updateView(currentMusic)


       }



        super.onResume()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requsetPermission()
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
            startForegroundService(Intent(this, MusicService::class.java))
        }else{
            startService(Intent(this, MusicService::class.java))

        }

    }

    private fun requsetPermission() {
        permissionList.clear()
        permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.let { permissionList.addAll(it) }
        if (permissionList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 0)
        } else {

            initView()
            initTimer()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                grantResults.find { it == PackageManager.PERMISSION_GRANTED }
                    ?.let {
                        requsetPermission()
                        return
                    }
                initView()
                initTimer()
            }
        }
    }

    @Subscribe(sticky = true)
    fun getMusicStatus(status:MediaPlayerStatus){
        isCheck=status.status
    }

    //获取service发送的progress数据并更新
    @Subscribe(sticky = true)
    fun onReceiveProgress(progressdata: ProgressData) {
        play_ctrl_btn.progress = progressdata.progress_data!!.toInt()
    }

    //获取currentposition并更新界面
    @Subscribe
    fun onReceiveMusic(serviceToMain: ServiceToMain) {
        currentMusic = music_lists?.get(serviceToMain.current)
        updateView(currentMusic)
    }

    //通用的更新界面方法
    fun updateView(music: Music?) {
        rotationS=0f
        music_title.text = music?.music_name
        music_artist.text = music?.music_artist
        var imgUri = Uri.parse(
            "content://media/external/audio/media/${music?.music_id}/albumart"
        )

        try {
            var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imgUri)
            ablum_image.setImageBitmap(bitmap)

        }catch (e:Exception){
            ablum_image.setImageDrawable(getDrawable(R.drawable.testimg))
            e.printStackTrace()
        }
        music_lists!!.find { it.isChecked===true
        }?.let {
            it.isChecked=false
        }
        music?.isChecked=true
        adapter?.notifyDataSetChanged()
    }



    override fun onPause() {
        super.onPause()
        Log.d(TAG,"OnPause")
    }

    fun initView() {

        ablum_image.setOnClickListener(this)
        play_ctrl_next.setOnClickListener(this)

        play_ctrl_btn.setOnCheckChangesListener { isChecked: Boolean ->
            if (isChecked) {
                Log.d(TAG, "播放")
                EventBus.getDefault().post(playCtrlType(playCtrlType.PLAY))

            } else {
                Log.d(TAG, "暂停")
                EventBus.getDefault().post(playCtrlType(playCtrlType.PAUSE))


            }
        }

        adapter = MusicListAdapter(music_lists!!, context = this)
        adapter?.setOnItemClickListener(object : MusicListAdapter.OnItemClickListener {
            override fun OnItemClick(v: View, position: Int) {

                EventBus.getDefault().post(MainToService(position))
                currentMusic=music_lists?.get(position)
                play_ctrl_btn.isChecked=true
                updateView(currentMusic)
                EventBus.getDefault().post(playCtrlType(playCtrlType.START))
            }
        })
        adapter?.setHasStableIds(true)
        music_list.layoutManager = LinearLayoutManager(this)
        music_list.adapter = adapter
    }

    fun initTimer() {
        timer = Timer("TimerRun")
        timer_imgrun = object : TimerTask() {
            override fun run() {
                    if (isCheck) {
                        ablum_image.pivotX = ablum_image.width / 2f
                        ablum_image.pivotY = ablum_image.height / 2f
                        ablum_image.rotation = rotationS
                        rotationS++
                        if (rotationS === 359f) {
                            rotationS = 0f
                        }
                }
            }
        }
        timer?.schedule(timer_imgrun, 0, 80)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        private const val TAG = "MainActivity"
        private val permissions = listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INSTALL_PACKAGES
        )
    }


}
