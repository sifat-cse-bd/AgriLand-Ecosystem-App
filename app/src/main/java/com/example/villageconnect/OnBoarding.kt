package com.example.villageconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class OnBoarding : AppCompatActivity() {
    lateinit var btnSignUp: Button
    lateinit var btnGetStarted: Button
    lateinit var card1: com.google.android.material.card.MaterialCardView
    lateinit var additionalLayout: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSignUp = findViewById(R.id.btnSignUp)

        btnGetStarted = findViewById(R.id.btnGetStarted)
        card1 = findViewById(R.id.card1)
        additionalLayout = findViewById(R.id.additionalLayout)

        card1.setOnClickListener {
            additionalLayout.visibility = View.VISIBLE
        }
        startRegistration()

    }


    fun startRegistration() {
        val clickListener = View.OnClickListener {
            startActivity(Intent(this, Registration::class.java))
        }

        btnGetStarted.setOnClickListener(clickListener)
        btnSignUp.setOnClickListener(clickListener)
    }
}