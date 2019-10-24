package com.wl.kotlinmusicdemo.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wl.kotlinmusicdemo.R

class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var behavior:BottomSheetBehavior<FrameLayout>?=null
    private val height:Int
    @SuppressLint("ServiceCast")
    get() {
        var height=1920
        if (context==null){
            val wm=context?.getSystemService(Context.WALLPAPER_SERVICE) as WindowManager
            val point=Point()
            if (wm!=null){
                wm.defaultDisplay.getSize(point)
                height=point.y
            }
        }
        return height
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
     if (context==null){
         return super.onCreateDialog(savedInstanceState)
     }
        return BottomSheetDialog(context!!,R.style.TransparentBottoSheetStyle)
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        var sheetDialog=dialog as BottomSheetDialog
        var frameLayout:FrameLayout?=sheetDialog?.delegate?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        if (frameLayout!=null){
            val layoutParams=frameLayout.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.height=height
            behavior= BottomSheetBehavior.from(frameLayout)
            behavior?.state=BottomSheetBehavior.STATE_EXPANDED
        }
    }

}