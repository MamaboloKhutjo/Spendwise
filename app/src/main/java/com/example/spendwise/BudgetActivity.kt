package com.example.spendwise

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class BudgetActivity : AppCompatActivity() {

    private var startDate: Calendar = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    private var endDate: Calendar = Calendar.getInstance()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displaySdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var etMinGoal: EditText
    private lateinit var etMaxGoal: EditText
    private lateinit var etMonthlyBudget: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)
        setupBottomNav()

        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        etMinGoal = findViewById(R.id.etMinGoal)
        etMaxGoal = findViewById(R.id.etMaxGoal)
        etMonthlyBudget = findViewById(R.id.etMonthlyBudget)
        val btnSaveGoals = findViewById<Button>(R.id.btnSaveGoals)

        etMinGoal.setText(AppData.getMinGoal().toString())
        etMaxGoal.setText(AppData.getMaxGoal().toString())
        etMonthlyBudget.setText(AppData.getMonthlyBudget().toString())

        updateDateLabels()

        tvStartDate.setOnClickListener { showDatePicker(startDate, true) }
        tvEndDate.setOnClickListener { showDatePicker(endDate, false) }

        btnSaveGoals.setOnClickListener {
            val min = etMinGoal.text.toString().toDoubleOrNull() ?: 0.0
            val max = etMaxGoal.text.toString().toDoubleOrNull() ?: 0.0
            val budget = etMonthlyBudget.text.toString().toDoubleOrNull() ?: 0.0
            
            AppData.setGoals(min, max)
            AppData.setMonthlyBudget(budget)
            
            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnAddBudget).setOnClickListener {
            startActivity(Intent(this, AddBudgetActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshBudgets()
    }

    private fun showDatePicker(calendar: Calendar, isStart: Boolean) {
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            updateDateLabels()
            refreshBudgets()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateLabels() {
        tvStartDate.text = displaySdf.format(startDate.time)
        tvEndDate.text = displaySdf.format(endDate.time)
    }

    private fun refreshBudgets() {
        val container = findViewById<LinearLayout>(R.id.budgetCategoryContainer)
        container.removeAllViews()

        val budgets = AppData.getAllBudgets()
        val startStr = sdf.format(startDate.time)
        val endStr = sdf.format(endDate.time)

        if (budgets.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No budgets set yet. Tap 'Add Budget' to create one."
            empty.setTextColor(Color.parseColor("#888888"))
            empty.setPadding(32, 24, 32, 24)
            container.addView(empty)
            return
        }

        for ((category, limit) in budgets) {
            val spent = AppData.getSpentForCategory(category, startStr, endStr)
            val progress = if (limit > 0) ((spent / limit) * 100).toInt().coerceIn(0, 100) else 0
            val isOverBudget = spent > limit

            // Card
            val card = CardView(this)
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.setMargins(0, 0, 0, 24)
            card.layoutParams = cardParams
            card.radius = 24f
            card.cardElevation = 8f
            card.setCardBackgroundColor(Color.WHITE)

            val inner = LinearLayout(this)
            inner.orientation = LinearLayout.VERTICAL
            inner.setPadding(40, 32, 40, 32)

            // Header row
            val headerRow = LinearLayout(this)
            headerRow.orientation = LinearLayout.HORIZONTAL

            val catText = TextView(this)
            catText.text = category
            catText.textSize = 16f
            catText.setTextColor(Color.parseColor("#1C1C1E"))
            catText.setTypeface(null, android.graphics.Typeface.BOLD)
            val catParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            catText.layoutParams = catParams

            val editText = TextView(this)
            editText.text = "Edit ›"
            editText.textSize = 13f
            editText.setTextColor(Color.parseColor("#888888"))
            editText.setOnClickListener {
                val intent = Intent(this, AddBudgetActivity::class.java)
                startActivity(intent)
            }

            headerRow.addView(catText)
            headerRow.addView(editText)

            // Progress bar
            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            val pbParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 24
            )
            pbParams.topMargin = 16
            pbParams.bottomMargin = 8
            progressBar.layoutParams = pbParams
            progressBar.max = 100
            progressBar.progress = progress
            
            // Try to find progress_bar_red or use fallback
            try {
                progressBar.progressDrawable = if (isOverBudget)
                    resources.getDrawable(resources.getIdentifier("progress_bar_red", "drawable", packageName), null)
                else
                    resources.getDrawable(resources.getIdentifier("progress_bar_orange", "drawable", packageName), null)
            } catch (e: Exception) {
                // Fallback if drawables not found
            }

            // Amount text
            val amountText = TextView(this)
            amountText.text = "R${formatAmount(spent)} / R${formatAmount(limit)}"
            amountText.textSize = 12f
            amountText.setTextColor(if (isOverBudget) Color.parseColor("#C62828") else Color.parseColor("#888888"))

            inner.addView(headerRow)
            inner.addView(progressBar)
            inner.addView(amountText)

            if (isOverBudget) {
                val warningText = TextView(this)
                warningText.text = "⚠ Over budget!"
                warningText.textSize = 12f
                warningText.setTextColor(Color.parseColor("#C62828"))
                warningText.setTypeface(null, android.graphics.Typeface.BOLD)
                inner.addView(warningText)
            }

            card.addView(inner)
            container.addView(card)
        }
    }

    private fun formatAmount(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            String.format("%,d", amount.toLong())
        } else {
            String.format("%,.2f", amount)
        }
    }

    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navDashboard).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        findViewById<LinearLayout>(R.id.navTransactions).setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        findViewById<LinearLayout>(R.id.navBudget).setOnClickListener { /* here */ }
        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        findViewById<ImageView>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
    }
}
