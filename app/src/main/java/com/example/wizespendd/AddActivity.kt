package com.example.wizespendd

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AddActivity allows users to record new expenses or income.
 * It handles data input, receipt image selection, and updates the user's logging streak.
 */
class AddActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    
    // Activity Result Launcher for picking an image from the gallery
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            try {
                // Request persistable permission to access the image even after app restarts
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {}
            Toast.makeText(this, "Receipt photo selected!", Toast.LENGTH_SHORT).show()
            // Change icon to indicate successful selection
            findViewById<ImageView>(R.id.ivUploadIcon)?.setImageResource(android.R.drawable.checkbox_on_background)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        // Initialize UI components
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val categoryDropdown = findViewById<AutoCompleteTextView>(R.id.categoryDropdown)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val btnUpload = findViewById<RelativeLayout>(R.id.btnUpload)

        // Set up the Category Dropdown using a simple array adapter
        val categories = arrayOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Other", "Income")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        categoryDropdown.setAdapter(adapter)

        // Handle receipt upload button click
        btnUpload.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Handle save button click
        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString()
            val category = categoryDropdown.text.toString()

            if (amountStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val amount = amountStr.toFloatOrNull() ?: 0f
                saveTransaction(category, amount)
                Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
                
                // Reset UI for next entry
                etAmount.setText("")
                categoryDropdown.setText("", false)
                selectedImageUri = null
                findViewById<ImageView>(R.id.ivUploadIcon)?.setImageResource(android.R.drawable.ic_menu_camera)
                
                // Refresh the streak UI after saving a transaction
                updateStreakUI() 
            }
        }

        setupNavigation()
        updateStreakUI() // Initial streak display check
        
        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Updates the streak UI cards to show current progress or a welcome message for first-timers.
     */
    private fun updateStreakUI() {
        val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
        val streak = sharedPrefs.getInt("user_streak", 0)
        val tvStreakHint = findViewById<TextView>(R.id.tvStreakHint)
        val tvStreakEmoji = findViewById<TextView>(R.id.tvStreakEmoji)

        if (streak == 0) {
            // First time experience: Encourage the user to start their streak
            tvStreakHint.text = "Welcome to WizeSpend! Log your first expense to start your logging streak."
            tvStreakEmoji.text = "✨"
        } else {
            // Existing streak: Show the fire emoji and current count
            tvStreakHint.text = "Awesome! You have a $streak-day logging streak. Keep going!"
            tvStreakEmoji.text = "🔥"
        }
    }

    /**
     * Persists the transaction data to SharedPreferences and updates streak logic.
     */
    private fun saveTransaction(category: String, amount: Float) {
        val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
        
        with(sharedPrefs.edit()) {
            if (category == "Income") {
                // Add to total income pool
                val currentIncome = sharedPrefs.getFloat("total_income", 0f)
                putFloat("total_income", currentIncome + amount)
            } else {
                // Record spending for specific category
                val key = "spent_$category"
                val currentTotal = sharedPrefs.getFloat(key, 0f)
                putFloat(key, currentTotal + amount)
                
                // Update grand total spent
                val grandTotal = sharedPrefs.getFloat("total_spent", 0f)
                putFloat("total_spent", grandTotal + amount)

                // Save image URI if a receipt was uploaded
                selectedImageUri?.let { uri ->
                    putString("image_$category", uri.toString())
                }

                // Streak Logic: Increments only once per day when an expense is logged
                val lastLogDate = sharedPrefs.getString("last_log_date", "")
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                if (lastLogDate != today) {
                    val currentStreak = sharedPrefs.getInt("user_streak", 0)
                    putInt("user_streak", currentStreak + 1)
                    putString("last_log_date", today)
                }
            }
            apply()
        }
    }

    /**
     * Configures the Bottom Navigation view behavior.
     */
    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navigation_add
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
                R.id.navigation_add -> true
                R.id.navigation_goals -> {
                    startActivity(Intent(this, BudgetActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    true
                }
                else -> false
            }
        }
    }
}