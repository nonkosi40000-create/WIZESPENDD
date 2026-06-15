package com.example.wizespendd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.regex.Pattern

/**
 * LoginActivity handles user authentication.
 * It validates the user's email and password format before allowing entry to the application.
 */
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvRegister = findViewById<View>(R.id.tvRegister)

        // Set up click listener for the login button
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Basic validation: Check if email contains '@' symbol
            if (!email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email address containing @", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Security check: Ensure password meets minimum complexity requirements
            if (!isValidPassword(password)) {
                Toast.makeText(this, "Password must be at least 8 characters, include uppercase, lowercase, a number, and a special character", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Authentication successful (simulated): Navigate to the home dashboard
            val intent = Intent(this, home::class.java)
            startActivity(intent)
            finish() // Prevent user from returning to login screen via back button
        }

        // Navigate to registration page if user doesn't have an account
        tvRegister.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    /**
     * Uses Regular Expression (Regex) to ensure the password is secure.
     * Requirements: 8+ chars, 1 digit, 1 lowercase, 1 uppercase, 1 special character.
     */
    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val passwordPattern = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\\S+$).{8,}$"
        )
        return passwordPattern.matcher(password).matches()
    }
}