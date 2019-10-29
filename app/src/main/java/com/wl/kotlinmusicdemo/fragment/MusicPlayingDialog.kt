package com.wl.kotlinmusicdemo.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wl.kotlinmusicdemo.R
import kotlinx.android.synthetic.main.music_playing_layout.*

class MusicPlayingDialog : BaseBottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        var options=BitmapFactory.Options()
        options.inSampleSize=3
        var bitmap=BitmapFactory.decodeResource(resources,R.drawable.defult_ablum_img,options)

        play_ground?.background=BitmapDrawable(resBitmap(bitmap))
    }

    fun resBitmap(bitmap: Bitmap):Bitmap{
        var inBitmap=bitmap
        var renderScript=RenderScript.create(activity)
        val input=Allocation.createFromBitmap(renderScript,inBitmap)
        val output=Allocation.createTyped(renderScript,input.type)
        val scriptIntrinsicBlur=ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        scriptIntrinsicBlur.setInput(input)
        scriptIntrinsicBlur.setRadius(25f)
        scriptIntrinsicBlur.forEach(output)
        output.copyTo(inBitmap)
        renderScript.destroy()

        return inBitmap
    }


}