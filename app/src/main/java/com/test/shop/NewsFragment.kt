package com.test.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class NewsFragment : Fragment() {

    companion object {
        val instance : NewsFragment by lazy { NewsFragment() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
        //第三的參數 false => 這個 layout 是否要在一開始出現時就直接去連接到 container 上
    }

}