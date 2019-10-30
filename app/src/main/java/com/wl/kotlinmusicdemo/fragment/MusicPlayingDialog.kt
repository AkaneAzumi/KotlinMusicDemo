package com.wl.kotlinmusicdemo.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur
import com.wl.kotlinmusicdemo.R
import com.wl.kotlinmusicdemo.databean.*
import com.wl.kotlinmusicdemo.musicmodel.Music
import com.wl.kotlinmusicdemo.utils.play_ctrl_type
import kotlinx.android.synthetic.main.music_playing_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MusicPlayingDialog : BaseBottomSheetDialogFragment(), View.OnClickListener {

    override fun onClick(p0: View?) {
        when (p0?.id) {
            ctrl_btn.id -> {
                if (isCheck) {
                    EventBus.getDefault().post(play_ctrl_type.PAUSE)
                } else {
                    EventBus.getDefault().post(play_ctrl_type.PLAY)
                }
            }
            next_btn.id -> {
                EventBus.getDefault().post(play_ctrl_type.NEXT)
            }
            pre_btn.id -> {
                EventBus.getDefault().post(play_ctrl_type.PREVIOUS)
            }
            play_ctrl_img.id -> {

            }
        }
    }
    private var timer: Timer? = null
    private var imaRotate: TimerTask? = null
    private var rotationS: Float = 0f
    private var isCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.music_playing_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctrl_btn.setOnClickListener(this)
        next_btn.setOnClickListener(this)
        pre_btn.setOnClickListener(this)
        playing_list.setOnClickListener(this)
        play_ctrl_img.setOnClickListener(this)
        progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                EventBus.getDefault().post(ProgressChange(p0!!.progress))
            }
        })
        initTimer()
    }


    private fun initTimer() {
        timer = Timer("Img Rotate")
        imaRotate = object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    if (isCheck) {
                        circleImageView?.pivotX = circleImageView.width / 2f
                        circleImageView?.pivotY = circleImageView.height / 2f
                        circleImageView?.rotation = rotationS
                        rotationS += 0.05f
                        if (rotationS === 359f) {
                            rotationS = 0f
                        }
                        ctrl_btn?.background = activity!!.resources.getDrawable(R.drawable.pause)
                    }else {
                        ctrl_btn?.background = activity!!.resources.getDrawable(R.drawable.play)
                    }
                }
            }
        }
        timer?.schedule(imaRotate, 0, 10)
    }


    private fun UpdateView(cunrrentMusic: Music) {
        rotationS = 0f
        playing_title.text = cunrrentMusic.music_name
        playing_artist.text = cunrrentMusic.music_artist
        max_time.text = cunrrentMusic.music_time
        try {
            var uri =
                Uri.parse("content://media/external/audio/media/${cunrrentMusic.music_id}/albumart")
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
            circleImageView.setImageBitmap(bitmap)
            val bitmap1 = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
            play_ground.background = BitmapDrawable(resBitmap(bitmap1))
        } catch (e: Exception) {
            circleImageView.setImageDrawable(resources.getDrawable(R.drawable.defult_ablum_img))
            var opition = BitmapFactory.Options()
            opition.inSampleSize = 40
            play_ground.background = BitmapDrawable(
                resBitmap(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.defult_ablum_img,
                        opition
                    )
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        releaseTimer()
    }

    fun releaseTimer() {
        imaRotate?.cancel()
        imaRotate = null
        timer?.cancel()
        timer = null
        Log.d("TAG", "TIMER IS RELEASE")
    }

    private fun resBitmap(bitmap: Bitmap): Bitmap {
        var inBitmap = Bitmap.createBitmap(bitmap)
        var renderScript = RenderScript.create(activity)
        val input = Allocation.createFromBitmap(renderScript, bitmap)
        val output = Allocation.createFromBitmap(renderScript, inBitmap)
        val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        scriptIntrinsicBlur.setInput(input)
        scriptIntrinsicBlur.setRadius((25).toFloat())
        scriptIntrinsicBlur.forEach(output)
        output.copyTo(inBitmap)
        renderScript.destroy()

        return inBitmap
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    open fun getProgress(progressData: ProgressToFragment) {
        progress?.progress = progressData.progresstoF
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    open fun getCunrrentTime(current: ServiceToFragment) {
        current_time?.text = current.currentiem
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED,sticky = true)
    fun getCurrentMusic(cunrrentMusic: Music) {
        UpdateView(cunrrentMusic)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun getMusicStaur(status: MediaPlayerStatusToFragment) {
        isCheck = status.status

    }


}