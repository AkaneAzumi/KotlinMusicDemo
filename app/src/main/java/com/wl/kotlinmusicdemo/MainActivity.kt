package com.wl.kotlinmusicdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.wl.kotlinmusicdemo.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private var viewModel:MainViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewModel()
    }

    fun initViewModel(){
        viewModel=ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

}
