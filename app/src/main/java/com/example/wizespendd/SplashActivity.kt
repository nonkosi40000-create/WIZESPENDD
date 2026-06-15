package com.example.wizespendd

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

/**
 * SplashActivity displays the initial branding screen.
 * It provides a smooth transition into the application while the system initializes.
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay execution for 3 seconds to show the logo/branding
        Handler(Looper.getMainLooper()).postDelayed({
            // Transition to the Get Started screen (MainActivity)
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Remove Splash from the back stack
        }, 3000)
    }
}