package com.wl.kotlinmusicdemo.ui

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wl.kotlinmusicdemo.R
import com.wl.kotlinmusicdemo.musicmodel.Music
import kotlinx.android.synthetic.main.playing_list_item.view.*

class PlayingListAdapter (private val musicList: MutableList<Music>, private val context: Context):RecyclerView.Adapter<PlayingListAdapter.PlayingViewHolder>(),View.OnClickListener{

    private var onItemClickListener:OnItemClickListener?=null
    override fun onClick(v: View?) {
        v?.let {  onItemClickListener?.onClick(it,it.tag as Int)}
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayingViewHolder {
        var v=View.inflate(context,R.layout.playing_list_item,null)
        v.setOnClickListener(this)
        return PlayingViewHolder(v)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: PlayingViewHolder, position: Int) {
       musicList?.let {
           holder.play_item.text="${it[position].music_name}--${it[position].music_artist}"
           if (it[position].isChecked!!) {
               holder.play_item.setTextColor(context.resources.getColor(R.color.main_color))
           }else{
               holder.play_item.setTextColor(Color.GRAY)

           }
       }
    }


    inner class PlayingViewHolder(view:View):RecyclerView.ViewHolder(view) {
        var play_item=view.playing_item
    }

    interface OnItemClickListener{
        fun onClick(item:View,position:Int)
    }
}