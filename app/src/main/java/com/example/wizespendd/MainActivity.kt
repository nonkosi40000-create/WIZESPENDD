package com.example.wizespendd

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * MainActivity serves as the landing or "Get Started" screen for new users.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the "Get Started" button and set navigation to the Login screen
        val btnGetStarted = findViewById<MaterialButton>(R.id.btnGetStarted)
        btnGetStarted.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close this activity so the user doesn't return here on back press
        }
    }
}