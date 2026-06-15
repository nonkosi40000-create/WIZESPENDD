package com.example.wizespendd

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

/**
 * GoalsActivity allows users to set their minimum and maximum monthly spending targets.
 * These goals are used throughout the app to determine if the user is "At Risk" or "Safe".
 */
class GoalsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navigation_goals

        val etMinGoal = findViewById<EditText>(R.id.etMinGoal)
        val etMaxGoal = findViewById<EditText>(R.id.etMaxGoal)
        val btnSaveGoals = findViewById<MaterialButton>(R.id.btnSaveMonthlyGoal)

        // Load existing goals from SharedPreferences to populate the fields
        val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
        etMinGoal.setText(sharedPrefs.getFloat("min_goal", 0f).toString())
        etMaxGoal.setText(sharedPrefs.getFloat("max_goal", 0f).toString())

        // Handle saving the updated goals
        btnSaveGoals.setOnClickListener {
            val min = etMinGoal.text.toString().toFloatOrNull() ?: 0f
            val max = etMaxGoal.text.toString().toFloatOrNull() ?: 0f

            // Validation: Max goal cannot be lower than the min goal
            if (max < min) {
                Toast.makeText(this, "Max goal must be greater than min goal", Toast.LENGTH_SHORT).show()
            } else {
                with(sharedPrefs.edit()) {
                    putFloat("min_goal", min)
                    putFloat("max_goal", max)
                    apply()
                }
                Toast.makeText(this, "Goals updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the standard bottom navigation menu
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
                R.id.navigation_goals -> true
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    true
                }
                else -> false
            }
        }
    }
}