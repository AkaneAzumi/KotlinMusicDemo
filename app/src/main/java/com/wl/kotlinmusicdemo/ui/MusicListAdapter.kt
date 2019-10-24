package com.wl.kotlinmusicdemo.ui

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*

class MusicListAdapter: RecyclerView.Adapter<MusicListAdapter.ViewHoler>() {
    override fun onBindViewHolder(holder: ViewHoler, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHoler {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    open class ViewHoler(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title_textView=itemView.list_title
        var artist_textView=itemView.list_artist
        var size_textView=itemView.list_size
        var time_textView=itemView.list_time
        init {
            itemView.setOnClickListener(View.OnClickListener {
                if (it!=null){

                }
            })
        }

    }

    

    interface OnClickListener{
        fun onClick()
    }

}