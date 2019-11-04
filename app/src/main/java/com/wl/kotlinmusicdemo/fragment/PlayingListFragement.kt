package com.wl.kotlinmusicdemo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wl.kotlinmusicdemo.R
import com.wl.kotlinmusicdemo.ui.PlayingListAdapter

class PlayingListFragement :BaseBottomSheetDialogFragment(){


    private lateinit var playingListAdapter: PlayingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.playing_list,container,false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        playingListAdapter= PlayingListAdapter(,this)
        super.onViewCreated(view, savedInstanceState)
    }
}