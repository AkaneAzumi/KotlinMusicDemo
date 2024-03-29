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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.wl.kotlinmusicdemo.databean.*
import com.wl.kotlinmusicdemo.fragment.MusicPlayingDialog
import com.wl.kotlinmusicdemo.musicmodel.MainViewModel
import com.wl.kotlinmusicdemo.musicmodel.Music
import com.wl.kotlinmusicdemo.musicmodel.getMusicListFromPhone
import com.wl.kotlinmusicdemo.service.MusicService
import com.wl.kotlinmusicdemo.ui.MusicListAdapter
import com.wl.kotlinmusicdemo.utils.SharedPrefrenceUtils
import com.wl.kotlinmusicdemo.utils.StatusbarUtil
import com.wl.kotlinmusicdemo.utils.playCtrlType

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.rightdrawlayout.*
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
            play_ctrl_next.id -> {
                play_ctrl_btn.isChecked=true
                EventBus.getDefault().post(playCtrlType(playCtrlType.NEXT))
            }
            ablum_image.id->{
                Log.d(TAG,"点击了头像")
                var musicPlayingDialog= MusicPlayingDialog()
                musicPlayingDialog.show(supportFragmentManager,"Dialog")
                EventBus.getDefault().postSticky(music_lists?.get(currentpositon))
            }
            quite.id->{
                stopService(Intent(this, MusicService::class.java))
                finish()

            }

        }
    }

    private lateinit var mainViewModel: MainViewModel
    private var currentpositon: Int = 0
    private var timer: Timer? = null
    private var timer_imgrun: TimerTask? = null
    private var isCheck: Boolean = false
    private var adapter: MusicListAdapter? = null
    private var rotationS: Float = 0f
    private var music_lists: MutableList<Music>? = null
    private var permissionList = mutableSetOf<String>()
    private var sharedPrefrenceUtils=SharedPrefrenceUtils(this)

    override fun onResume() {
        if(!EventBus.getDefault().isRegistered(this))
        {
            EventBus.getDefault().register(this)

        }
        sharedPrefrenceUtils?.getData("Music",Music())?.let {
            updateView(it as Music)
        }
        sharedPrefrenceUtils?.getData("imgUri",Uri.parse(""))?.let{
            var imguri=Uri.parse(it as String )
            background_image.setImageURI(imguri)
        }
        super.onResume()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusbarUtil.setRootViewFitsSystemWindows(this,false)
        StatusbarUtil.setTranslucentStatus(this)
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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN_ORDERED)
    fun setBackGround(imageUri: BackGroundUri){
        background_image.setImageURI(imageUri.imageUri)
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
        Log.d(TAG,"shoudaoshuju")
        currentpositon = serviceToMain.current
        updateView(music_lists!!.get(currentpositon))
    }

    //通用的更新界面方法
    fun updateView(music: Music) {
        rotationS=0f
        music_title.text = music.music_name
        music_artist.text = music.music_artist
        var imgUri = Uri.parse(
            "content://media/external/audio/media/${music.music_id}/albumart"
        )
        try {
            var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imgUri)
                ablum_image.setImageBitmap(bitmap)

        }catch (e:Exception){
            ablum_image.setImageDrawable(getDrawable(R.drawable.testimg))

            e.printStackTrace()
        }

            music_lists?.find { it.isChecked===true
            }?.let {
                it.isChecked=false
            }
            music.isChecked=true
            adapter?.notifyDataSetChanged()


        EventBus.getDefault().post(MainToService(currentpositon))
    }



    override fun onPause() {
        super.onPause()
        Log.d(TAG,"OnPause")
        EventBus.getDefault().unregister(this)

    }

    fun initView() {
        mainViewModel=ViewModelProviders.of(this).get(MainViewModel::class.java)
        mainViewModel.imageUri.observe(this, androidx.lifecycle.Observer {
            background_image.setImageURI(it)
            sharedPrefrenceUtils.SaveData("imgUri",it)
        })
        music_lists = getMusicListFromPhone(this)

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
                this@MainActivity.currentpositon = position
                play_ctrl_btn.isChecked=true
                updateView(music_lists!!.get(position))
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
