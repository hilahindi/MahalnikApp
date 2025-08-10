package com.example.mahalapp.activities

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.mahalapp.R

class MainActivity : BaseActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBaseContent(R.layout.activity_main)

    }
}