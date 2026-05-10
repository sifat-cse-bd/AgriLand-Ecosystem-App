package com.example.villageconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

@Suppress("INFERRED_TYPE_VARIABLE_INTO_EMPTY_INTERSECTION_WARNING")
class SplashScreen : AppCompatActivity() {
    lateinit var logo: ImageView
    lateinit var appName: LinearLayout
    lateinit var topAnimation: Animation
    lateinit var bottomAnimation: Animation
    lateinit var progessBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        // Android system theme theke app theme-e switch
        setTheme(R.style.Theme_VillageConnect)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Window settings
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_splash_screen)

        // ProgressBar initialize (Age invisible thakbe)
        progessBar = findViewById<ProgressBar>(R.id.progressBar)
        progessBar.visibility = View.INVISIBLE

        startAnimation()
    }

    fun startAnimation() {
        logo = findViewById(R.id.logo)
        appName = findViewById(R.id.appName)
        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        logo.startAnimation(topAnimation)
        appName.startAnimation(bottomAnimation)

        // Animation end listener
        bottomAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                startProgressBar()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
    fun startProgressBar() {
        progessBar.visibility = View.VISIBLE
        progessBar.max = 100

        val duration = 500L // 3 sec

        val timer = object : android.os.CountDownTimer(duration, 30) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((duration - millisUntilFinished) * 100 / duration).toInt()
                progessBar.progress = progress
            }

            override fun onFinish() {
                progessBar.progress = 100

                startActivity(Intent(this@SplashScreen, OnBoarding::class.java))
               finish()
            }
        }

        timer.start()
    }
}