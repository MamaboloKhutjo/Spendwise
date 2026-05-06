package com.example.spendwise

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = sdf.format(cal.time)
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endOfMonth = sdf.format(cal.time)

        val currentMonthTransactions = AppData.getTransactionsInPeriod(startOfMonth, endOfMonth)
        val income = currentMonthTransactions.filter { it.type == "income" }.sumOf { it.amount }
        val expenses = currentMonthTransactions.filter { it.type == "expense" }.sumOf { it.amount }

        val monthlyBudget = AppData.getMonthlyBudget()
        val moneyLeft = monthlyBudget - expenses

        val minGoal = AppData.getMinGoal()
        val maxGoal = AppData.getMaxGoal()

        // Update Header Stats
        findViewById<TextView>(R.id.tvMoneyLeft).text = "R ${formatAmount(moneyLeft)}"
        findViewById<TextView>(R.id.tvMonthlyBudget).text = "R ${formatAmount(monthlyBudget)}"
        findViewById<TextView>(R.id.tvTotalSpent).text = "R ${formatAmount(expenses)}"

        // Progress Bar
        val progressBar = findViewById<ProgressBar>(R.id.budgetProgress)
        if (monthlyBudget > 0) {
            val progress = ((expenses / monthlyBudget) * 100).toInt().coerceIn(0, 100)
            progressBar.progress = progress
        } else {
            progressBar.progress = 0
        }

        // Income/Expense Cards
        findViewById<TextView>(R.id.tvIncome).text = "+R${formatAmount(income)}"
        findViewById<TextView>(R.id.tvExpense).text = "-R${formatAmount(expenses)}"

        // Goal Advice
        val adviceText = findViewById<TextView>(R.id.tvGoalAdvice)
        if (maxGoal > 0) {
            when {
                expenses > maxGoal -> {
                    adviceText.text = "⚠️ You have exceeded your maximum spending goal!"
                    adviceText.setTextColor(android.graphics.Color.parseColor("#C62828"))
                }
                expenses < minGoal -> {
                    adviceText.text = "👍 You are well below your minimum spending goal. Great job!"
                    adviceText.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                }
                else -> {
                    adviceText.text = "Keep track of your spending to stay within your goals."
                    adviceText.setTextColor(android.graphics.Color.parseColor("#666666"))
                }
            }
        } else {
            adviceText.text = "Set your goals in the Budget tab to track progress!"
            adviceText.setTextColor(android.graphics.Color.parseColor("#666666"))
        }

        // Recent transactions list
        val transactions = AppData.getTransactions().take(4)
        val container = findViewById<LinearLayout>(R.id.transactionContainer)
        container.removeAllViews()

        if (transactions.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No transactions yet. Tap + to add one."
            empty.setTextColor(android.graphics.Color.parseColor("#888888"))
            empty.setPadding(32, 16, 32, 16)
            container.addView(empty)
        } else {
            for (t in transactions) {
                val row = layoutInflater.inflate(R.layout.item_transaction_row, container, false)
                row.findViewById<TextView>(R.id.txnName).text = t.category
                row.findViewById<TextView>(R.id.txnDate).text = t.date
                val amountView = row.findViewById<TextView>(R.id.txnAmount)
                if (t.type == "expense") {
                    amountView.text = "-R${formatAmount(t.amount)}"
                    amountView.setTextColor(android.graphics.Color.parseColor("#C62828"))
                } else {
                    amountView.text = "+R${formatAmount(t.amount)}"
                    amountView.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                }
                
                row.setOnClickListener {
                    // Start TransactionsActivity or show dialog
                    val intent = Intent(this, TransactionsActivity::class.java)
                    startActivity(intent)
                }

                container.addView(row)
            }
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
        findViewById<LinearLayout>(R.id.navDashboard).setOnClickListener { }
        findViewById<LinearLayout>(R.id.navTransactions).setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
            overridePendingTransition(0, 0)
        }
        findViewById<LinearLayout>(R.id.navBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
            overridePendingTransition(0, 0)
        }
        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }
        findViewById<ImageView>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
    }
}
