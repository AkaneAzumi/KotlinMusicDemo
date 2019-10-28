package com.wl.kotlinmusicdemo

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.wl.kotlinmusicdemo.musicmodel.Music
import com.wl.kotlinmusicdemo.musicmodel.getMusicListFromPhone
import com.wl.kotlinmusicdemo.ui.MusicListAdapter
import com.wl.kotlinmusicdemo.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity(),View.OnClickListener {



    override fun onClick(p0: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        when(p0?.id){
            play_ctrl_next.id->if (position!=0){
                position++
            }
        }
    }

    private val positon:Int=0
    private var timer:Timer?=null
    private var timer_imgrun:TimerTask?=null
    private var position:Int=0
    private var viewModel:MainViewModel?=null
    private var isCheck:Boolean=false
    private var adapter:MusicListAdapter?=null
    private var rotationS:Float=0f
    private var music_lists:MutableList<Music>?=null
    private var permissionList= mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requsetPermission()

    }

    private fun requsetPermission(){
        permissionList.clear()
        permissions.filter {
            ContextCompat.checkSelfPermission(this,it)!= PackageManager.PERMISSION_GRANTED
        }.let { permissionList.addAll(it) }
        if (permissionList.isNotEmpty()){
            ActivityCompat.requestPermissions(this,permissionList.toTypedArray(),0)
        }else{
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
        when(requestCode){
            0->{
                grantResults.find { it==PackageManager.PERMISSION_GRANTED }
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

    fun initViewModel(){
        viewModel=ViewModelProviders.of(this).get(MainViewModel::class.java)
        music_lists= getMusicListFromPhone(this)
    }

    fun initView(){
        play_ctrl_btn.setOnCheckChangesListener { isChecked: Boolean -> isCheck = !isChecked
        }
        adapter= MusicListAdapter(music_lists!!,context = this)
        adapter?.setOnItemClickListener(object : MusicListAdapter.OnItemClickListener {
            override fun OnItemClick(v: View, position: Int) {
                this@MainActivity.position=positon
            }
        })
        music_list.layoutManager=LinearLayoutManager(this)
        music_list.adapter=adapter
    }

    fun initTimer(){
        timer=Timer("TimerRun")

        timer_imgrun=object :TimerTask(){
            override fun run() {
               runOnUiThread {
                   if (isCheck){
                       ablum_image.pivotX=ablum_image.width/2f
                       ablum_image.pivotY=ablum_image.height/2f
                       ablum_image.rotation=rotationS
                       rotationS++
                       if (rotationS===359f){
                           rotationS=0f
                       }
                   }
               }
            }
        }

        timer?.schedule(timer_imgrun,0,80)
    }

    companion object{
        private val permissions= listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }


}
