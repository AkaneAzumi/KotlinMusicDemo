package com.wl.kotlinmusicdemo.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.wl.kotlinmusicdemo.musicmodel.Music
import java.sql.Savepoint

class SharedPrefrenceUtils(context: Context?) {
    private var context:Context? = context
    private var MUSIC_DATA="Music_Data"
    fun SaveData(key:String,any: Any){
        var sharedPreferences=context?.getSharedPreferences(MUSIC_DATA,Context.MODE_PRIVATE)
        var editor=sharedPreferences?.edit()
        var gson=Gson()
        if (any is Music){
            var json=gson.toJson(any)
            editor?.putString(key,json)
        }else if (any is Int){
            editor?.putInt(key,any.toInt())
        }else if (any is Uri){
            editor?.putString(key,any.toString())
        }
        editor?.commit()
    }

    fun getData(key: String,any: Any): Any? {
        var sharedPreferences=context?.getSharedPreferences(MUSIC_DATA,Context.MODE_PRIVATE)
        var gson=Gson()
        if (any is Music){
            var json=sharedPreferences?.getString(key, String())
            var music=gson.fromJson(json,Music::class.java)
            return music
        }else if (any is Int){
            var code=sharedPreferences?.getInt(key,0)
            return code
        }else if (any is Uri){
            var imgUri=sharedPreferences?.getString(key,"")
            return  imgUri
        }
        return null
    }

}