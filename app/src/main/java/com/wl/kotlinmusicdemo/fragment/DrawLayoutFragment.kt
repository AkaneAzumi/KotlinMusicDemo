package com.wl.kotlinmusicdemo.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.linchaolong.android.imagepicker.ImagePicker
import com.linchaolong.android.imagepicker.cropper.CropImage
import com.wl.kotlinmusicdemo.R
import com.wl.kotlinmusicdemo.databean.BackGroundUri
import com.wl.kotlinmusicdemo.musicmodel.MainViewModel
import com.wl.kotlinmusicdemo.service.MusicService
import kotlinx.android.synthetic.main.rightdrawlayout.*
import org.greenrobot.eventbus.EventBus

class DrawLayoutFragment :Fragment(),View.OnClickListener{
    override fun onClick(v: View?) {
        when(v?.id){
            quite.id->{
                activity?.let {
                    it.stopService(Intent(it, MusicService::class.java))
                    it.finish()
                }
            }
            back_setting.id->{
                imagePicker.setTitle("设置背景图")
                imagePicker.setCropImage(true)
                imagePicker.startChooser(this,imageCallbacks)

            }
        }
    }

    private lateinit var mainViewModel: MainViewModel
    private var imagePicker=ImagePicker()
    private val imageCallbacks=object :ImagePicker.Callback(){
        override fun onPickImage(imageUri: Uri?) {
        }

        override fun onCropImage(imageUri: Uri?) {
            super.onCropImage(imageUri)
            Log.d("TEST","$imageUri")
            mainViewModel.postimageUrl(imageUri!!)
        }

        override fun cropConfig(builder: CropImage.ActivityBuilder?) {
            builder!!.setRequestedSize(1080,1920)
                .setAspectRatio(9,16)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.rightdrawlayout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        quite.setOnClickListener(this)
        back_setting.setOnClickListener(this)
        activity?.let {
            mainViewModel=ViewModelProviders.of(it).get(MainViewModel::class.java)
        }
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePicker.onActivityResult(this,requestCode,resultCode,data)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imagePicker.onRequestPermissionsResult(this,requestCode, permissions, grantResults)
    }
}