package com.wl.kotlinmusicdemo.musicmodel

import android.content.Context
import android.provider.MediaStore
import com.wl.kotlinmusicdemo.utils.sizeFormat
import com.wl.kotlinmusicdemo.utils.timeFormat

fun getMusicListFromPhone(context:Context):List<Music>{
    val list:MutableList<Music> = ArrayList()
    val cursor=context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,
        MediaStore.Audio.Media.DEFAULT_SORT_ORDER)
    if (cursor!=null){
        while (cursor.moveToNext()){
            val music= Music().let {
                it.music_name=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                it.music_ablum=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                it.music_ablumId=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                it.music_artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                it.music_size= sizeFormat(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)))
                it.music_time= timeFormat(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)))
                it.music_artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                it.music_id=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                it.music_url=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                if (cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC))!=0){
                    list.add(it)
                }
            }
        }
    }
    return list
}