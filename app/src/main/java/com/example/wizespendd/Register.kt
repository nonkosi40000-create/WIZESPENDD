package com.example.wizespendd

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.regex.Pattern

/**
 * Register Activity allows new users to create an account.
 * It collects personal information, validates it, and initializes user preferences.
 */
class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvLogin = findViewById<View>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val fullName = etFullName.text.toString().trim()

            // Ensure name is provided
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Basic email validation
            if (!email.contains("@")) {
                Toast.makeText(this, "Email must contain @", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Phone number format validation (South African standard 10 digits)
            if (phone.length != 10 || !phone.all { it.isDigit() }) {
                Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Password security check
            if (!isValidPassword(password)) {
                Toast.makeText(this, "Password must be at least 8 characters, include uppercase, lowercase, a number, and a special character", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Persist initial user profile data locally
            val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putString("profile_username", fullName)
                putString("profile_email", email)
                putString("profile_phone", phone)
                // Requirement: Initializing streak at 0 for new users to trigger "first time" message in AddActivity
                putInt("user_streak", 0) 
                apply()
            }

            // Proceed to the main dashboard
            val intent = Intent(this, home::class.java)
            startActivity(intent)
            finish()
        }

        // Navigate back to login if user already has an account
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Validates password strength using Regex.
     */
    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val passwordPattern = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\\S+$).{8,}$"
        )
        return passwordPattern.matcher(password).matches()
    }
}