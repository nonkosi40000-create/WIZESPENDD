package com.example.wizespendd

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import java.util.Locale

/**
 * AiInsightsActivity provides personalized financial coaching and advice.
 * It uses the user's spending data to generate actionable steps and responds to natural language queries.
 */
class AiInsightsActivity : AppCompatActivity() {

    private lateinit var llStepsContainer: LinearLayout
    private lateinit var etAiQuestion: EditText
    private lateinit var tvAiResponse: TextView
    private lateinit var btnAskAi: MaterialButton
    private lateinit var tvCoachStatus: TextView
    private lateinit var tvAiSavedAmount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_insights)

        // Initialize Views
        llStepsContainer = findViewById(R.id.llStepsContainer)
        etAiQuestion = findViewById(R.id.etAiQuestion)
        tvAiResponse = findViewById(R.id.tvAiResponse)
        btnAskAi = findViewById(R.id.btnAskAi)
        tvCoachStatus = findViewById(R.id.tvCoachStatus)
        tvAiSavedAmount = findViewById(R.id.tvAiSavedAmount)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navigation_ai

        setupNavigation(bottomNav)
        refreshAiCoach() // Generate initial insights based on current data

        // Handle AI query submission
        btnAskAi.setOnClickListener {
            val question = etAiQuestion.text.toString().trim()
            if (question.isNotEmpty()) {
                processAiQuestion(question)
                etAiQuestion.setText("")
            } else {
                Toast.makeText(this, "Please enter a question for the coach", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Refreshes the AI Coach's advice based on stored spending and income data.
     * It generates a step-by-step roadmap for the user.
     */
    private fun refreshAiCoach() {
        llStepsContainer.removeAllViews()
        val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)

        val totalSpent = sharedPrefs.getFloat("total_spent", 0f)
        val totalIncome = sharedPrefs.getFloat("total_income", 0f)
        val minGoal = sharedPrefs.getFloat("min_goal", 1000f)
        val maxGoal = sharedPrefs.getFloat("max_goal", 5000f)

        val spentFood = sharedPrefs.getFloat("spent_Food", 0f)
        val spentTransport = sharedPrefs.getFloat("spent_Transport", 0f)
        val spentShop = sharedPrefs.getFloat("spent_Shopping", 0f)
        val spentBills = sharedPrefs.getFloat("spent_Bills", 0f)
        val spentEnt = sharedPrefs.getFloat("spent_Entertainment", 0f)
        val spentOther = sharedPrefs.getFloat("spent_Other", 0f)

        val netSavings = (totalIncome - totalSpent).coerceAtLeast(0f)
        tvAiSavedAmount.text = "R${String.format(Locale.US, "%.2f", netSavings)}"

        val steps = mutableListOf<Pair<String, String>>()

        // Conditional logic for generating specific financial advice
        when {
            totalIncome == 0f -> {
                tvCoachStatus.text = "Action Required: Define your budget"
                steps.add("Step 1: Log Your Income" to "I can't guide you without knowing your monthly earnings. Go to the 'Add' page and select 'Income' category.")
                steps.add("Step 2: Set Financial Goals" to "Visit the 'Goals' page to set your minimum and maximum spending targets.")
            }
            totalSpent > totalIncome -> {
                tvCoachStatus.text = "Emergency: Negative Cash Flow"
                steps.add("Step 1: Immediate Freeze" to "You have spent R${String.format("%.2f", totalSpent - totalIncome)} over your income. Stop all non-essential purchases immediately.")
                steps.add("Step 2: Identify Fixed Costs" to "Review your 'Bills' (R${String.format("%.2f", spentBills)}) and see if any can be negotiated or delayed.")
                steps.add("Step 3: Cut Entertainment" to "Your entertainment spend is R${String.format("%.2f", spentEnt)}. Cutting this could recover significant funds.")
            }
            totalSpent > maxGoal -> {
                tvCoachStatus.text = "Warning: Budget Limit Exceeded"
                steps.add("Step 1: Categorize Overspending" to "You are R${String.format("%.2f", totalSpent - maxGoal)} above your maximum goal. Find which category is highest.")
                steps.add("Step 2: The 10% Rule" to "Try to reduce your spending in the next 3 days by 10% in every category.")
            }
            totalSpent > minGoal -> {
                tvCoachStatus.text = "Opportunity: Optimization Needed"
                steps.add("Step 1: Tighten the Belt" to "You are within range but could save R${String.format("%.2f", totalSpent - minGoal)} more by reaching your ideal minimum target.")
                steps.add("Step 2: Track Small Leaks" to "Look at your 'Other' category (R${String.format("%.2f", spentOther)}). These are often small costs that add up.")
            }
            totalSpent > 0 -> {
                tvCoachStatus.text = "Status: Wealth Building Mode"
                steps.add("Step 1: Pay Yourself First" to "Great job staying under R${String.format("%.0f", minGoal)}! Move R${String.format("%.2f", netSavings * 0.5)} to a high-interest savings account.")
                steps.add("Step 2: Invest in Knowledge" to "Since you have a surplus, consider reading a book on personal finance to grow your wealth further.")
            }
            else -> {
                tvCoachStatus.text = "Welcome! Let's build your plan"
                steps.add("Step 1: Track 3 Days" to "Spend as you normally would for 3 days and log everything. This creates your baseline.")
                steps.add("Step 2: Categorize" to "Make sure to use the correct categories so I can give you precise advice.")
            }
        }

        // Additional Category Intelligence
        if (totalSpent > 0) {
            if (spentFood > totalSpent * 0.35) {
                steps.add("💡 Food Strategy" to "Food is ${String.format("%.0f", (spentFood/totalSpent)*100)}% of your spending. Meal prepping on Sundays could save you up to R500/month.")
            }
            if (spentShop > 300f) {
                steps.add("💡 Shopping Pause" to "You've spent R${String.format("%.2f", spentShop)} on shopping. Wait 24 hours before your next non-essential purchase.")
            }
        }

        // Add generated steps to the UI
        for (step in steps) {
            addStepToUi(step.first, step.second)
        }
    }

    /**
     * Dynamically adds a step (title and description) to the scrollable container.
     */
    private fun addStepToUi(title: String, desc: String) {
        val inflater = LayoutInflater.from(this)
        val stepView = inflater.inflate(android.R.layout.simple_list_item_2, llStepsContainer, false)
        val text1 = stepView.findViewById<TextView>(android.R.id.text1)
        val text2 = stepView.findViewById<TextView>(android.R.id.text2)

        text1.text = title
        text1.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        text1.textSize = 15f
        text1.setTypeface(null, Typeface.BOLD)

        text2.text = desc
        text2.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        text2.textSize = 13f

        stepView.setPadding(0, 16, 0, 16)
        llStepsContainer.addView(stepView)

        // Add a visual divider between steps
        val divider = View(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
        params.setMargins(0, 8, 0, 8)
        divider.layoutParams = params
        divider.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        divider.alpha = 0.2f
        llStepsContainer.addView(divider)
    }

    /**
     * Simulates "thinking" time before providing a response to the user's question.
     */
    private fun processAiQuestion(question: String) {
        tvAiResponse.text = "Thinking..."
        tvAiResponse.postDelayed({
            provideAiResponse(question.lowercase())
        }, 800)
    }

    /**
     * Basic NLP logic to map user keywords to specific financial advice.
     */
    private fun provideAiResponse(q: String) {
        val sharedPrefs = getSharedPreferences("WizeSpendPrefs", Context.MODE_PRIVATE)
        val totalSpent = sharedPrefs.getFloat("total_spent", 0f)
        val totalIncome = sharedPrefs.getFloat("total_income", 0f)
        val minGoal = sharedPrefs.getFloat("min_goal", 1000f)
        val maxGoal = sharedPrefs.getFloat("max_goal", 5000f)
        
        val spentFood = sharedPrefs.getFloat("spent_Food", 0f)
        val spentShop = sharedPrefs.getFloat("spent_Shopping", 0f)
        val spentEnt = sharedPrefs.getFloat("spent_Entertainment", 0f)
        val savings = (totalIncome - totalSpent).coerceAtLeast(0f)

        val response = when {
            q.contains("hello") || q.contains("hi") || q.contains("hey") -> {
                "Hello! I'm your WizeSpend AI Coach. I can help you analyze your spending habits (R${String.format("%.2f", totalSpent)}) and help you reach your goals. What would you like to know?"
            }
            q.contains("how") && q.contains("improve") -> {
                if (totalSpent > maxGoal) {
                    "To improve, we need to cut R${String.format("%.2f", totalSpent - maxGoal)} to get back to your max goal. I suggest looking at your Shopping (R${String.format("%.2f", spentShop)}) first."
                } else {
                    "You're doing well! To improve further, try to reduce 'Other' expenses and increase your monthly savings from R${String.format("%.2f", savings)}."
                }
            }
            q.contains("goal") || q.contains("reach") -> {
                val toGo = if (totalSpent < minGoal) minGoal - totalSpent else 0f
                if (toGo > 0) {
                    "To reach your minimum goal of R${String.format("%.0f", minGoal)}, you have R${String.format("%.2f", toGo)} left to spend this month. Spend wisely!"
                } else {
                    "You've passed your minimum goal. Now, focus on not exceeding your R${String.format("%.0f", maxGoal)} maximum limit."
                }
            }
            q.contains("save") || q.contains("money") -> {
                "Based on your data, your biggest savings opportunity is in ${getHighestCategory(sharedPrefs)}. Also, your current net savings is R${String.format("%.2f", savings)}. Try to increase this by 5% next month."
            }
            q.contains("food") || q.contains("grocery") || q.contains("eat") -> {
                "You've spent R${String.format("%.2f", spentFood)} on food. If this is high, try 'Meatless Mondays' or buying generic brands to save roughly 15% on your bill."
            }
            q.contains("shopping") || q.contains("buy") -> {
                "Shopping total: R${String.format("%.2f", spentShop)}. Ask yourself: 'Will I still care about this item in 3 days?' If not, don't buy it."
            }
            q.contains("entertainment") || q.contains("fun") -> {
                "Entertainment is at R${String.format("%.2f", spentEnt)}. Look for free local events or park visits to have fun without spending!"
            }
            q.contains("broke") || q.contains("no money") || q.contains("overspend") -> {
                if (totalSpent > totalIncome) {
                    "It looks like you've spent R${String.format("%.2f", totalSpent - totalIncome)} more than your income. We need to create an emergency plan. Start by listing all upcoming bills."
                } else {
                    "You're not broke yet! You have R${String.format("%.2f", savings)} left. Let's make it last until next payday."
                }
            }
            q.contains("tips") || q.contains("advice") -> {
                "Here is my top tip for you: The 50/30/20 rule. 50% for Needs, 30% for Wants, 20% for Savings. Currently, your savings rate is ${if(totalIncome>0) String.format("%.0f", (savings/totalIncome)*100) else "0"}%."
            }
            else -> {
                "I'm not quite sure about that. But looking at your total spending of R${String.format("%.2f", totalSpent)}, I'd recommend checking if your 'Other' category is getting too high."
            }
        }
        tvAiResponse.text = response
    }
    
    /**
     * Finds the category with the highest spending for targeted advice.
     */
    private fun getHighestCategory(prefs: android.content.SharedPreferences): String {
        val categories = mapOf(
            "Food" to prefs.getFloat("spent_Food", 0f),
            "Transport" to prefs.getFloat("spent_Transport", 0f),
            "Shopping" to prefs.getFloat("spent_Shopping", 0f),
            "Bills" to prefs.getFloat("spent_Bills", 0f),
            "Entertainment" to prefs.getFloat("spent_Entertainment", 0f)
        )
        return categories.maxByOrNull { it.value }?.key ?: "your expenses"
    }

    /**
     * Sets up Bottom Navigation View item selection.
     */
    private fun setupNavigation(bottomNav: BottomNavigationView) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, home::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                    true
                }
                R.id.navigation_ai -> true
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
    }
}