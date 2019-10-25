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

class MusicListAdapter(musicList: MutableList<Music>,context: Context): RecyclerView.Adapter<MusicListAdapter.ViewHoler>(),View.OnClickListener {

    private var onItemClickListener:OnItemClickListener?=null
    private var musicList:MutableList<Music>?=null
    private var context:Context?=null
    init {
        this.musicList=musicList
        this.context=context
    }

    override fun onBindViewHolder(holder: ViewHoler, position: Int) {
            holder.itemView.setOnClickListener(View.OnClickListener {
                onItemClickListener?.OnItemClick(holder.itemView,holder.layoutPosition)
            })
        if (musicList?.get(position)?.isChecked!!){
            holder.artist_textView?.setTextColor(context!!.resources.getColor(R.color.main_color))
            holder.time_textView?.setTextColor(context!!.resources.getColor(R.color.main_color))
            holder.title_textView?.setTextColor(context!!.resources.getColor(R.color.main_color))
            holder.size_textView?.setTextColor(context!!.resources.getColor(R.color.main_color))
        }else{
            holder.artist_textView?.setTextColor(Color.GRAY)
            holder.time_textView?.setTextColor(Color.GRAY)
            holder.title_textView?.setTextColor(Color.GRAY)
            holder.size_textView?.setTextColor(Color.GRAY)
        }

    }

    override fun getItemCount(): Int {
        return musicList!!.size
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

    private fun setOnItemClickListener(onItemClickListener: OnItemClickListener){
        this.onItemClickListener=onItemClickListener
    }
     interface OnItemClickListener{
        fun OnItemClick(v:View,position: Int)
    }

    override fun onClick(v: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        onItemClickListener?.OnItemClick(v!!,v!!.tag as Int)
    }

}