package com.wl.kotlinmusicdemo.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.time.format.DecimalStyle
import java.util.jar.Attributes

class TitleTextView : AppCompatTextView {

    constructor(context: Context):super(context)

    constructor(context: Context,attributes: AttributeSet):super(context,attributes)

    constructor(context: Context,attributes: AttributeSet,defStyle:Int):super(context,attributes,defStyle)

    override fun isFocused(): Boolean {
        return true
    }
}