package com.wl.kotlinmusicdemo.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.wl.kotlinmusicdemo.R
import com.wl.kotlinmusicdemo.databean.ProgressChange
import com.wl.kotlinmusicdemo.databean.ServiceToFragment
import com.wl.kotlinmusicdemo.musicmodel.Music
import com.wl.kotlinmusicdemo.utils.ProgressData
import com.wl.kotlinmusicdemo.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.music_playing_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception
import java.util.*

class MusicPlayingDialog : BaseBottomSheetDialogFragment() {
    private lateinit var viewModel:MainViewModel
    private var timer:Timer?=null
    private var imaRotate:TimerTask?=null
    private var rotationS:Float=0f
    private var isCheck=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return  inflater.inflate(R.layout.music_playing_layout,container,false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
             viewModel=ViewModelProviders.of(it).get(MainViewModel::class.java)
         }
        subscribeData()
        progress.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                EventBus.getDefault().post(ProgressChange(p0!!.progress))

            }
        })
    }


    fun subscribeData(){
        viewModel?.currentMusic.observe(this, Observer {
            it?.let {
                UpdateView(it)
            }
        })
    }

    fun initTimer(){
        timer= Timer("Img Rotate")
        imaRotate=object :TimerTask(){
            override fun run() {
                activity?.runOnUiThread {
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

        timer?.schedule(imaRotate,0,80)
    }



    fun UpdateView(cunrrentMusic:Music){
        playing_title.text=cunrrentMusic.music_name
        playing_artist.text=cunrrentMusic.music_artist
        max_time.text=cunrrentMusic.music_time
        try {
            var uri=Uri.parse("content://media/external/audio/media/${cunrrentMusic.music_id}/albumart")
            val bitmap=MediaStore.Images.Media.getBitmap(activity?.contentResolver,uri)
            circleImageView.setImageBitmap(bitmap)

            val bitmap1=MediaStore.Images.Media.getBitmap(activity?.contentResolver,uri)
            play_ground.background=BitmapDrawable(resBitmap(bitmap1))
        }catch (e:Exception){
            circleImageView.setImageDrawable(resources.getDrawable(R.drawable.defult_ablum_img))
            var opition=BitmapFactory.Options()
            opition.inSampleSize=40
            play_ground.background=BitmapDrawable(resBitmap(BitmapFactory.decodeResource(resources,R.drawable.defult_ablum_img,opition)))
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    fun resBitmap(bitmap: Bitmap):Bitmap{
        var inBitmap=Bitmap.createBitmap(bitmap)
        var renderScript=RenderScript.create(activity)
        val input=Allocation.createFromBitmap(renderScript,bitmap)
        val output=Allocation.createFromBitmap(renderScript,inBitmap)
        val scriptIntrinsicBlur=ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        scriptIntrinsicBlur.setInput(input)
        scriptIntrinsicBlur.setRadius((25).toFloat())
        scriptIntrinsicBlur.forEach(output)
        output.copyTo(inBitmap)
        renderScript.destroy()

        return inBitmap
    }

    @Subscribe(threadMode= ThreadMode.MAIN)
    open fun getProgress(progressData: ProgressData){
        progress.progress=progressData.progress_data!!.toInt()
    }

    @Subscribe(threadMode= ThreadMode.MAIN)
    open fun getCunrrentTime(current:ServiceToFragment){
        current_time.text=current.currentiem
    }

    @Subscribe
    fun getCurrentMusic(cunrrentMusic: Music){
        UpdateView(cunrrentMusic)
    }


}