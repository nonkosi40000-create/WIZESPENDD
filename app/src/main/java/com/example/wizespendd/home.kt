package com.example.wizespendd

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import java.util.Calendar

/**
 * home activity serves as the main dashboard for the user.
 * it displays spending summaries, budget progress, category-specific details, and rewards.
 */
class home : AppCompatActivity() {

    private lateinit var cardFood: MaterialCardView
    private lateinit var cardTransport: MaterialCardView
    private lateinit var cardShopping: MaterialCardView
    private lateinit var cardOther: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize category cards for visibility filtering
        cardFood = findViewById(R.id.cardFood)
        cardTransport = findViewById(R.id.cardTransport)
        cardShopping = findViewById(R.id.cardShopping)
        cardOther = findViewById(R.id.cardOther)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navigation_home

        setupNavigation(bottomNav)
        setupSearch()
        
        findViewById<View>(R.id.cvHomeProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the dashboard data whenever the user returns to this screen
        updateUI() 
    }

    /**
     * Configures the search bar to filter spending categories.
     */
    private fun setupSearch() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterCategories(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCategories(newText)
                return true
            }
        })
    }

    /**
     * Filters the category cards based on keywords related to spending.
     */
    private fun filterCategories(query: String?) {
        val q = query?.lowercase() ?: ""
        
        if (q.isEmpty()) {
            cardFood.visibility = View.VISIBLE
            cardTransport.visibility = View.VISIBLE
            cardShopping.visibility = View.VISIBLE
            cardOther.visibility = View.VISIBLE
            return
        }

        // Mapping keywords to categories for an "intelligent" search experience
        val foodKeywords = listOf("food", "eat", "grocery", "lunch", "dinner", "breakfast", "snack", "restaurant", "cafe", "coffee", "pizza", "burger", "drink", "apple", "fruit", "milk", "bread")
        val transportKeywords = listOf("transport", "travel", "car", "bus", "taxi", "fuel", "gas", "petrol", "uber", "bolt", "train", "flight", "commute", "bike", "cycle")
        val shoppingKeywords = listOf("shopping", "buy", "store", "clothes", "mall", "fashion", "shoes", "tech", "gadget", "phone", "gift", "present", "shirt", "jeans", "makeup")
        val otherKeywords = listOf("other", "bills", "entertainment", "fun", "movie", "rent", "wifi", "internet", "gym", "health", "doctor", "fees", "game", "party", "club", "hair")

        val foodMatch = foodKeywords.any { it.contains(q) } || q.contains("food")
        val transportMatch = transportKeywords.any { it.contains(q) } || q.contains("transport")
        val shoppingMatch = shoppingKeywords.any { it.contains(q) } || q.contains("shopping")
        val otherMatch = otherKeywords.any { it.contains(q) } || q.contains("other") || q.contains("bills") || q.contains("entertainment")

        cardFood.visibility = if (foodMatch) View.VISIBLE else View.GONE
        cardTransport.visibility = if (transportMatch) View.VISIBLE else View.GONE
        cardShopping.visibility = if (shoppingMatch) View.VISIBLE else View.GONE
        cardOther.visibility = if (otherMatch) View.VISIBLE else View.GONE
    }

    /**
     * Fetches the latest data from SharedPreferences and updates the dashboard visuals.
     */
    private fun updateUI() {
        val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
        
        // 1. Personalized Greeting
        val userName = sharedPrefs.getString("profile_username", "User")
        val greeting = getGreeting()
        findViewById<TextView>(R.id.tvGreeting).text = greeting
        findViewById<TextView>(R.id.tvWelcomeName).text = userName

        // 2. Streak Counter: Shows consistency in logging
        val userStreak = sharedPrefs.getInt("user_streak", 0)
        findViewById<TextView>(R.id.tvStreak).text = "$userStreak-Day Streak"
        val tvHeaderStreak = findViewById<TextView>(R.id.tvHeaderStreak)
        tvHeaderStreak.text = "🔥 $userStreak"
        tvHeaderStreak.visibility = if (userStreak > 0) View.VISIBLE else View.GONE

        // 3. Profile Picture Loading: Handles the user's custom photo or initial
        val savedImageUri = sharedPrefs.getString("profile_image_uri", null)
        val ivHomeProfile = findViewById<ImageView>(R.id.ivHomeProfile)
        val tvHomeProfileInitial = findViewById<TextView>(R.id.tvHomeProfileInitial)

        if (savedImageUri != null) {
            try {
                ivHomeProfile.setImageURI(Uri.parse(savedImageUri))
                ivHomeProfile.visibility = View.VISIBLE
                ivHomeProfile.imageTintList = null
                tvHomeProfileInitial.visibility = View.GONE
            } catch (e: Exception) {
                ivHomeProfile.visibility = View.GONE
                tvHomeProfileInitial.visibility = View.VISIBLE
                tvHomeProfileInitial.text = if (!userName.isNullOrEmpty()) userName.substring(0, 1).uppercase() else "U"
            }
        } else {
            ivHomeProfile.visibility = View.GONE
            tvHomeProfileInitial.visibility = View.VISIBLE
            tvHomeProfileInitial.text = if (!userName.isNullOrEmpty()) userName.substring(0, 1).uppercase() else "U"
        }

        // 4. Budget Progress Bar: Visualizes spending vs. income/goals
        val totalSpent = sharedPrefs.getFloat("total_spent", 0f)
        val totalIncome = sharedPrefs.getFloat("total_income", 0f)
        val minGoal = sharedPrefs.getFloat("min_goal", 1000f)
        val maxGoal = sharedPrefs.getFloat("max_goal", 5000f)

        val tvProgress = findViewById<TextView>(R.id.tvBudgetProgress)
        val pbBudget = findViewById<ProgressBar>(R.id.pbBudget)
        val displayLimit = if (totalIncome > 0) totalIncome else maxGoal
        tvProgress.text = "Monthly Budget Progress (R${String.format("%.2f", totalSpent)} / R${String.format("%.2f", displayLimit)})"
        val progressPercent = if (displayLimit > 0) (totalSpent / displayLimit * 100).toInt() else 0
        pbBudget.progress = progressPercent.coerceIn(0, 100)

        // 5. Update Category Totals
        findViewById<TextView>(R.id.tvSpentFood).text = "R${String.format("%.2f", sharedPrefs.getFloat("spent_Food", 0f))}"
        findViewById<TextView>(R.id.tvSpentTransport).text = "R${String.format("%.2f", sharedPrefs.getFloat("spent_Transport", 0f))}"
        findViewById<TextView>(R.id.tvSpentShop).text = "R${String.format("%.2f", sharedPrefs.getFloat("spent_Shopping", 0f))}"
        findViewById<TextView>(R.id.tvSpentOther).text = "R${String.format("%.2f", sharedPrefs.getFloat("spent_Other", 0f) + sharedPrefs.getFloat("spent_Entertainment", 0f) + sharedPrefs.getFloat("spent_Bills", 0f))}"

        // 6. Display Receipt Previews if they exist
        updateCategoryImage(findViewById(R.id.ivCategoryFood), sharedPrefs.getString("image_Food", null))
        updateCategoryImage(findViewById(R.id.ivCategoryTransport), sharedPrefs.getString("image_Transport", null))
        updateCategoryImage(findViewById(R.id.ivCategoryShopping), sharedPrefs.getString("image_Shopping", null))
        updateCategoryImage(findViewById(R.id.ivCategoryOther), sharedPrefs.getString("image_Other", null))

        // 7. Dynamic Gamification Badges based on spending health
        val ivBadge = findViewById<ImageView>(R.id.ivBadgeBudget)
        val tvBadge = findViewById<TextView>(R.id.tvBadgeTitle)
        when {
            totalSpent == 0f -> {
                tvBadge.text = "New Starter"
                ivBadge.imageTintList = ColorStateList.valueOf(Color.GRAY)
            }
            totalSpent < minGoal -> {
                tvBadge.text = "Budget Pro (Gold)"
                ivBadge.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFD700"))
            }
            totalSpent <= maxGoal -> {
                tvBadge.text = "On Track (Green)"
                ivBadge.imageTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            }
            else -> {
                tvBadge.text = "Over Budget (Red)"
                ivBadge.imageTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
            }
        }
    }

    /**
     * Determines the greeting string based on the current time of day.
     */
    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good Morning,"
            in 12..13 -> "Good day,"
            in 14..17 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
    }

    /**
     * Helper to load and display saved receipt images in category thumbnails.
     */
    private fun updateCategoryImage(imageView: ImageView, uriString: String?) {
        if (uriString != null) {
            try {
                val uri = Uri.parse(uriString)
                imageView.setImageURI(uri)
                imageView.visibility = View.VISIBLE
            } catch (e: Exception) {
                imageView.visibility = View.GONE
            }
        } else {
            imageView.visibility = View.GONE
        }
    }

    /**
     * Set up the Bottom Navigation and Floating Action Button click listeners.
     */
    private fun setupNavigation(bottomNav: BottomNavigationView) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
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
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    true
                }
                else -> false
            }
        }
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }
    }
}