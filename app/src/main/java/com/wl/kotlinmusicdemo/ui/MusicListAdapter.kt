package com.wl.kotlinmusicdemo.ui

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wl.kotlinmusicdemo.R
import com.wl.kotlinmusicdemo.musicmodel.Music
import kotlinx.android.synthetic.main.list_item.view.*

class MusicListAdapter(private val musicList: MutableList<Music>,private val context: Context): RecyclerView.Adapter<MusicListAdapter.ViewHoler>(),View.OnClickListener {

    private var onItemClickListener:OnItemClickListener?=null


    override fun getItemViewType(position: Int): Int {
        return position
    }


    override fun onBindViewHolder(holder: ViewHoler, position: Int) {
            holder.itemView.setOnClickListener(View.OnClickListener {
                onItemClickListener?.OnItemClick(holder.itemView,holder.layoutPosition)
            })
        holder.size_textView.text=musicList[position].music_size
        holder.title_textView.text=musicList[position].music_name
        holder.time_textView.text=musicList[position].music_time
        holder.artist_textView.text=musicList[position].music_artist
        if (musicList?.get(position)?.isChecked!!){
            holder.artist_textView?.setTextColor(context!!.resources.getColor(R.color.main_color))
            holder.title_textView.isSelected=true
            holder.time_textView?.setTextColor(context!!.resources.getColor(R.color.main_color))
            holder.title_textView?.setTextColor(context!!.resources.getColor(R.color.main_color))
            holder.size_textView?.setTextColor(context!!.resources.getColor(R.color.main_color))
        }else{
            holder.artist_textView?.setTextColor(Color.GRAY)
            holder.title_textView.isSelected=false
            holder.time_textView?.setTextColor(Color.GRAY)
            holder.title_textView?.setTextColor(Color.GRAY)
            holder.size_textView?.setTextColor(Color.GRAY)
        }

    }

    override fun getItemCount(): Int {
        return musicList!!.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHoler {
        var v=View.inflate(context,R.layout.list_item,null)
        v.setOnClickListener(this)
        return  ViewHoler(v)
    }

    inner class ViewHoler(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title_textView=itemView.list_title
        var artist_textView=itemView.list_artist
        var size_textView=itemView.list_size
        var time_textView=itemView.list_time


    }

    open fun setOnItemClickListener(onItemClickListener: OnItemClickListener){
        this.onItemClickListener=onItemClickListener
    }
     interface OnItemClickListener{
        fun OnItemClick(v:View,position: Int)
    }

    override fun onClick(v: View?) {
        onItemClickListener?.OnItemClick(v!!,v!!.tag as Int)
    }

}