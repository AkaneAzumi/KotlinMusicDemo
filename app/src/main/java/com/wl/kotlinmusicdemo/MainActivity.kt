package com.wl.kotlinmusicdemo

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.wl.kotlinmusicdemo.databean.MainToService
import com.wl.kotlinmusicdemo.musicmodel.Music
import com.wl.kotlinmusicdemo.musicmodel.getMusicListFromPhone
import com.wl.kotlinmusicdemo.service.MusicService
import com.wl.kotlinmusicdemo.ui.MusicListAdapter
import com.wl.kotlinmusicdemo.utils.ProgressData
import com.wl.kotlinmusicdemo.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception
import java.util.*

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity(), View.OnClickListener {


    //按键点击事件监听
    override fun onClick(p0: View?) {
        when (p0?.id) {
            play_ctrl_next.id -> if (currentpositon != 0) {
                currentpositon++
                updateView()
                play_ctrl_btn.isChecked=true
                EventBus.getDefault().post(play_ctrl_type.NEXT)
            }
        }
    }

    private var currentpositon: Int = 0
    private var timer: Timer? = null
    private var timer_imgrun: TimerTask? = null
    private var viewModel: MainViewModel? = null
    private var isCheck: Boolean = false
    private var adapter: MusicListAdapter? = null
    private var rotationS: Float = 0f
    private var music_lists: MutableList<Music>? = null
    private var permissionList = mutableSetOf<String>()

    enum class play_ctrl_type {
        NEXT,
        PREVIOUS,
        PAUSE,
        PLAY,
        START
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
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

            initViewModel()
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
                initViewModel()
                initView()
                initTimer()
            }
        }
    }

    //获取service发送的progress数据并更新
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveProgress(progressdata: ProgressData) {
        play_ctrl_btn.progress = progressdata.progress_data!!.toInt()
    }

    //获取currentposition并更新界面
    @Subscribe
    fun onReceiveMusic(position: Int) {
        currentpositon = position
        updateView()
    }

    //通用的更新界面方法
    fun updateView() {
        isCheck=true
        music_title.text = music_lists?.get(currentpositon)!!.music_name
        music_artist.text = music_lists?.get(currentpositon)!!.music_artist
        var imgUri = Uri.parse(
            "content://media/external/audio/media/${music_lists!![currentpositon].music_id}/albumart"
        )
        try {
            var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imgUri)
                ablum_image.setImageBitmap(bitmap)

        }catch (e:Exception){
            ablum_image.setImageDrawable(getDrawable(R.drawable.defult_ablum_img))

            e.printStackTrace()
        }


        music_lists!!.find { it.isChecked===true
        }?.let {
            it.isChecked=false
        }
        music_lists!![currentpositon].isChecked=true
        adapter?.notifyDataSetChanged()
        EventBus.getDefault().post(MainToService(currentpositon))
    }


    //初始化iewmodel
    fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        music_lists = getMusicListFromPhone(this)
        initObserver()
    }

    fun initObserver() {
    }

    fun initView() {
        play_ctrl_next.setOnClickListener(this)
        play_ctrl_btn.setOnCheckChangesListener { isChecked: Boolean ->
            if (isChecked) {
                Log.d(TAG, "播放")
                isCheck=true
                EventBus.getDefault().post(play_ctrl_type.PLAY)

            } else {
                Log.d(TAG, "暂停")
                isCheck=false
                EventBus.getDefault().post(play_ctrl_type.PAUSE)


            }
        }
        adapter = MusicListAdapter(music_lists!!, context = this)
        adapter?.setOnItemClickListener(object : MusicListAdapter.OnItemClickListener {
            override fun OnItemClick(v: View, position: Int) {
                this@MainActivity.currentpositon = position
                play_ctrl_btn.isChecked=true
                updateView()
                EventBus.getDefault().post(play_ctrl_type.START)
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
                runOnUiThread {
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
        }
        timer?.schedule(timer_imgrun, 0, 80)
    }


    companion object {
        private const val TAG = "MainActivity"
        private val permissions = listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }


}
