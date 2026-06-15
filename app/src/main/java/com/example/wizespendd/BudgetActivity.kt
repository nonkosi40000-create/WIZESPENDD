package com.example.wizespendd

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup

/**
 * BudgetActivity displays spending analytics through interactive bar charts.
 * It provides Weekly, Monthly, and Yearly views with AI-driven risk assessments.
 */
class BudgetActivity : AppCompatActivity() {

    private lateinit var barsContainer: LinearLayout
    private lateinit var tvChartTitle: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvDetailInfo: TextView
    private lateinit var tvAiAdvice: TextView
    private lateinit var toggleGroup: MaterialButtonToggleGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        // Initialize UI components
        barsContainer = findViewById(R.id.barsContainer)
        tvChartTitle = findViewById(R.id.tvChartTitle)
        tvStatus = findViewById(R.id.tvStatus)
        tvDetailInfo = findViewById(R.id.tvDetailInfo)
        tvAiAdvice = findViewById(R.id.tvAiAdvice)
        toggleGroup = findViewById(R.id.toggleGroup)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navigation_goals
        setupNavigation(bottomNav)

        // Navigate to GoalsActivity to edit budget limits
        findViewById<ImageButton>(R.id.btnEditGoals).setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }

        // Handle toggle between Week, Month, and Year views
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnWeek -> updateWeeklyView()
                    R.id.btnMonth -> updateMonthlyView()
                    R.id.btnYear -> updateYearlyView()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Default view on entry is Monthly
        updateMonthlyView()
    }

    /**
     * Renders the weekly spending graph.
     * Highlights days that exceed the daily budget allocation.
     */
    private fun updateWeeklyView() {
        tvChartTitle.text = "Weekly Spending Analysis"
        barsContainer.removeAllViews()
        
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
        val maxGoal = sharedPrefs.getFloat("max_goal", 5000f)
        val dailyBudget = maxGoal / 30f // Simplified daily limit
        
        val totalSpent = sharedPrefs.getFloat("total_spent", 0f)
        // Simulate a base spend spread over the week for visualization
        val baseSpend = (totalSpent / 7).coerceAtLeast(10f)

        var weeklyTotal = 0f
        for (i in days.indices) {
            // Simulate higher spending on weekends
            val daySpend = if (i >= 5) baseSpend * 1.6f else baseSpend * 0.8f
            val isAtRisk = daySpend > dailyBudget
            addBarToChart(days[i], daySpend, dailyBudget * 2.5f, isAtRisk)
            weeklyTotal += daySpend
        }

        val weeklyLimit = dailyBudget * 7
        val isWeeklyAtRisk = weeklyTotal > weeklyLimit
        
        // Update risk status and AI advice
        tvStatus.text = "Status: ${if (isWeeklyAtRisk) "At Risk - Pacing High" else "Safe - Within Limits"}"
        tvStatus.setTextColor(if (isWeeklyAtRisk) Color.RED else Color.parseColor("#2E7D32"))
        tvDetailInfo.text = "Weekly Total: R${String.format("%.2f", weeklyTotal)} / Limit: R${String.format("%.2f", weeklyLimit)}"
        
        tvAiAdvice.text = if (isWeeklyAtRisk) {
            "AI Advice: Your weekend spending is putting you at risk. To stay safe, implement a 'Cash-Only' rule for social outings to avoid the problem of invisible overspending. Stop all non-essential buys for 48 hours to recover."
        } else {
            "AI Advice: You are safe! To avoid future problems, set aside R${String.format("%.0f", dailyBudget * 0.2f)} today from your unspent daily budget. This builds a safety net for unexpected expenses next week."
        }
    }

    /**
     * Renders the monthly category breakdown.
     * Highlights categories that consume more than 35% of the budget.
     */
    private fun updateMonthlyView() {
        tvChartTitle.text = "Monthly Category Analytics"
        barsContainer.removeAllViews()

        val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
        val maxGoal = sharedPrefs.getFloat("max_goal", 5000f)

        val categories = mapOf(
            "Food" to sharedPrefs.getFloat("spent_Food", 0f),
            "Trans" to sharedPrefs.getFloat("spent_Transport", 0f),
            "Shop" to sharedPrefs.getFloat("spent_Shopping", 0f),
            "Other" to (sharedPrefs.getFloat("spent_Other", 0f) + sharedPrefs.getFloat("spent_Bills", 0f) + sharedPrefs.getFloat("spent_Entertainment", 0f))
        )

        val totalSpent = categories.values.sum()
        val chartScale = maxOf(maxGoal, totalSpent, 1000f)

        for ((name, amount) in categories) {
            // A category is 'at risk' if it eats up too much of the total budget
            val isAtRisk = amount > (maxGoal * 0.35f)
            addBarToChart(name, amount, chartScale, isAtRisk)
        }

        val isOverBudget = totalSpent > maxGoal
        tvStatus.text = if (isOverBudget) "Status: Monthly Budget At Risk" else "Status: Monthly Budget Safe"
        tvStatus.setTextColor(if (isOverBudget) Color.RED else Color.parseColor("#2E7D32"))
        tvDetailInfo.text = "Total Spent: R${String.format("%.2f", totalSpent)} / Goal: R${String.format("%.0f", maxGoal)}"
        
        tvAiAdvice.text = if (isOverBudget) {
            "AI Advice: You have exceeded your safety zone. To avoid problems, review your highest category (red bar) and cut it by 50% next month. For now, use generic brands for groceries to stay safe until payday."
        } else {
            "AI Advice: Great job staying safe! To avoid problems later, don't increase your spending just because you are under budget. Transfer R500 to your savings now to lock in this month's success."
        }
    }

    /**
     * Renders the yearly risk assessment using simulated and real data.
     * Clearly identifies 'Safe' vs 'At Risk' months to help with long-term planning.
     */
    private fun updateYearlyView() {
        tvChartTitle.text = "Yearly Risk Assessment"
        barsContainer.removeAllViews()

        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
        val maxGoal = sharedPrefs.getFloat("max_goal", 5000f)
        val totalSpent = sharedPrefs.getFloat("total_spent", 0f)

        // Mock data for previous months to show trends
        val simulatedMonthlySpends = floatArrayOf(
            maxGoal * 0.85f, // Jan - Safe (Below max goal)
            maxGoal * 1.25f, // Feb - At Risk (Above max goal)
            maxGoal * 0.90f, // Mar - Safe
            totalSpent       // Apr - Current Month (Real data)
        )

        var hasYearlyRisk = false
        for (i in months.indices) {
            val amount = if (i < simulatedMonthlySpends.size) simulatedMonthlySpends[i] else 0f
            val isAtRisk = amount > maxGoal
            if (isAtRisk) hasYearlyRisk = true
            
            // Render the bar with color coding: Red for Risk, Green for Safe
            if (i < simulatedMonthlySpends.size) {
                addBarToChart(months[i], amount, maxGoal * 1.5f, isAtRisk)
            } else {
                addBarToChart(months[i], 0f, maxGoal * 1.5f, false)
            }
        }

        tvStatus.text = if (hasYearlyRisk) "Status: Annual Trend - At Risk" else "Status: Annual Trend - Safe"
        tvStatus.setTextColor(if (hasYearlyRisk) Color.RED else Color.parseColor("#2E7D32"))
        tvDetailInfo.text = "Identify high-risk months (Red bars) to avoid yearly budget failure."
        
        // AI advice focused on long-term safety and problem avoidance
        tvAiAdvice.text = "AI Advice: To stay safe year-round, you must identify 'High Risk' months (like holidays) in advance. To avoid problems, save 15% extra during 'Safe' (Green) months. This creates a buffer that prevents debt during expensive seasons."
    }

    /**
     * Helper function to dynamically create and add a bar to the chart container.
     */
    private fun addBarToChart(label: String, amount: Float, scale: Float, isAtRisk: Boolean) {
        val barLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }

        val chartHeightPx = (180 * resources.displayMetrics.density).toInt()
        val barHeight = ((amount / scale) * chartHeightPx).toInt().coerceAtLeast(4)

        val barView = View(this).apply {
            layoutParams = LinearLayout.LayoutParams((22 * resources.displayMetrics.density).toInt(), barHeight).apply {
                bottomMargin = (4 * resources.displayMetrics.density).toInt()
            }
            // Red for risk, Green for safe
            setBackgroundColor(if (isAtRisk) Color.parseColor("#F44336") else Color.parseColor("#0BA84A"))
            alpha = 0.85f
        }

        val labelTv = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            text = label
            textSize = 9f
            setTextColor(Color.parseColor("#6B7280"))
        }

        barLayout.addView(barView)
        barLayout.addView(labelTv)
        barsContainer.addView(barLayout)
    }

    private fun setupNavigation(bottomNav: BottomNavigationView) {
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