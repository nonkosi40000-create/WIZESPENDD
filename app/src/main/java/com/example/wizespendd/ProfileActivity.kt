package com.example.wizespendd

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * ProfileActivity manages user profile information and customization.
 * Users can update their personal details and upload a profile picture.
 */
class ProfileActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvProfileInitial: TextView

    // Result launcher for picking a profile picture from device storage
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivProfilePic.setImageURI(it)
            ivProfilePic.visibility = View.VISIBLE
            tvProfileInitial.visibility = View.GONE
            
            // Persist the image URI immediately to SharedPreferences
            val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("profile_image_uri", it.toString()).apply()
            
            // Request long-term permission to the image
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navigation_profile

        // Initialize UI components
        val etUsername = findViewById<EditText>(R.id.etProfileUsername)
        val etPhone = findViewById<EditText>(R.id.etProfilePhone)
        val etAddress = findViewById<EditText>(R.id.etProfileAddress)
        val etDOB = findViewById<EditText>(R.id.etProfileDOB)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveProfile)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        val profileImageCard = findViewById<MaterialCardView>(R.id.profileImageCard)
        
        ivProfilePic = findViewById(R.id.ivProfilePic)
        tvProfileInitial = findViewById(R.id.tvProfileInitial)
        val tvDisplayUsername = findViewById<TextView>(R.id.tvDisplayUsername)
        val tvDisplayEmail = findViewById<TextView>(R.id.tvDisplayEmail)

        // Load and display current profile data from local storage
        val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPrefs.getString("profile_username", "User")
        val savedEmail = sharedPrefs.getString("profile_email", "user@example.com")
        val savedPhone = sharedPrefs.getString("profile_phone", "")
        val savedAddress = sharedPrefs.getString("profile_address", "")
        val savedDOB = sharedPrefs.getString("profile_dob", "")
        val savedImageUri = sharedPrefs.getString("profile_image_uri", null)

        etUsername.setText(savedUsername)
        etPhone.setText(savedPhone)
        etAddress.setText(savedAddress)
        etDOB.setText(savedDOB)
        
        tvDisplayUsername.text = savedUsername
        tvDisplayEmail.text = savedEmail
        
        // Logic to show profile image or a fallback initial
        if (savedImageUri != null) {
            ivProfilePic.setImageURI(Uri.parse(savedImageUri))
            ivProfilePic.visibility = View.VISIBLE
            tvProfileInitial.visibility = View.GONE
        } else {
            tvProfileInitial.text = if (savedUsername != null && savedUsername.isNotEmpty()) savedUsername.substring(0, 1).uppercase() else "U"
            ivProfilePic.visibility = View.GONE
            tvProfileInitial.visibility = View.VISIBLE
        }

        // Trigger gallery picker when clicking on the profile image area
        profileImageCard.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Save updated profile text fields
        btnSave.setOnClickListener {
            val newUsername = etUsername.text.toString()
            val newPhone = etPhone.text.toString()
            val newAddress = etAddress.text.toString()
            val newDOB = etDOB.text.toString()

            with(sharedPrefs.edit()) {
                putString("profile_username", newUsername)
                putString("profile_phone", newPhone)
                putString("profile_address", newAddress)
                putString("profile_dob", newDOB)
                apply()
            }

            // Sync visual header with new data
            tvDisplayUsername.text = newUsername
            if (ivProfilePic.visibility == View.GONE) {
                tvProfileInitial.text = if (newUsername.isNotEmpty()) newUsername.substring(0, 1).uppercase() else "U"
            }
            
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }

        // Handle account logout
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            // Clear activity stack so user cannot go back to profile after logging out
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Set up Bottom Navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, home::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    true
                }
                R.id.navigation_ai -> {
                    startActivity(Intent(this, AiInsightsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    true
                }
                R.id.navigation_add -> {
                    startActivity(Intent(this, AddActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    true
                }
                R.id.navigation_goals -> {
                    startActivity(Intent(this, BudgetActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    true
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }
}