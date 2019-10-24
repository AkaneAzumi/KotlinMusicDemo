package com.wl.kotlinmusicdemo.utils

import java.math.BigDecimal
import java.text.DateFormatSymbols
import java.text.DecimalFormat

fun timeFormat(time:Int):String{
    var ss:String=DecimalFormat("00").format(time/1000%60)
    var mm:String=DecimalFormat("00").format(time/(1000*60))
    var t:String="$mm:$ss"
    return t
}

fun sizeFormat(size:Int):String{
    var big:BigDecimal= BigDecimal(size)
    var tem:Double=big.setScale(2,BigDecimal.ROUND_HALF_UP).toDouble()
    return tem as String
}