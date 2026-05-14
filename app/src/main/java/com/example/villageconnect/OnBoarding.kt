package com.example.villageconnect

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.villageconnect.auth.Login
import com.example.villageconnect.auth.Registration
import com.example.villageconnect.fragments.AppOverview
import kotlin.jvm.java

class OnBoarding : AppCompatActivity() {
    lateinit var btnSkip: TextView
    lateinit var btnGetStarted: Button
    lateinit var btnOverview: Button

    lateinit var bgVideo: VideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.navigationBarColor = Color.TRANSPARENT
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_onboarding)


        val mainLayout = findViewById<FrameLayout>(R.id.main)
        //val bottomButtons = findViewById<LinearLayout>(R.id.bottomButtonsLayout)

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(0, 0, 0, 0)

            playVideoBackground()
            insets

        }




        btnSkip = findViewById(R.id.btnSkip)
        btnGetStarted = findViewById(R.id.btnGetStarted)
        btnOverview = findViewById(R.id.btnOverview)
        bgVideo = findViewById(R.id.bgVideo)



        btnSkip.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
        btnGetStarted.setOnClickListener {
            startActivity(Intent(this, Registration::class.java))
            finish()
        }
        btnOverview.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, AppOverview())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun playVideoBackground() {
        val videoUri = Uri.parse("android.resource://$packageName/${R.raw.onboarding_film}")
        bgVideo.setVideoURI(videoUri)

        bgVideo.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            mediaPlayer.setVolume(0f, 0f)



            bgVideo.start()
        }
    }

    override fun onResume() {
        super.onResume()
        bgVideo.start()
    }

    override fun onPause() {
        super.onPause()
        bgVideo.pause()
    }
}