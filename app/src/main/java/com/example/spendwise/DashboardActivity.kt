package com.example.spendwise

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
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

        // 1. Calculate All-time Totals (The "Total amount we input")
        val allTransactions = AppData.getTransactions()
        val totalIncomeAllTime = allTransactions.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpenseAllTime = allTransactions.filter { it.type == "expense" }.sumOf { it.amount }
        val availableBalance = totalIncomeAllTime - totalExpenseAllTime

        // 2. Calculate Monthly Stats
        val currentMonthTransactions = AppData.getTransactionsInPeriod(startOfMonth, endOfMonth)
        val monthlyIncome = currentMonthTransactions.filter { it.type == "income" }.sumOf { it.amount }
        val monthlyExpense = currentMonthTransactions.filter { it.type == "expense" }.sumOf { it.amount }
        val monthlyNet = monthlyIncome - monthlyExpense

        val monthlyBudget = AppData.getMonthlyBudget()
        
        // 3. Update Header UI
        // Balance reflects (Total Income - Total Expense)
        findViewById<TextView>(R.id.tvMoneyLeft).text = "R ${formatAmount(availableBalance)}"
        findViewById<TextView>(R.id.tvMonthlyIncomeHeader)?.text = "R ${formatAmount(monthlyIncome)}"
        findViewById<TextView>(R.id.tvTotalSpent).text = "R ${formatAmount(monthlyExpense)}"
        findViewById<TextView>(R.id.tvMonthlyBudget).text = "R ${formatAmount(monthlyBudget)}"

        // Update Progress Bar
        val progressBar = findViewById<ProgressBar>(R.id.budgetProgress)
        if (monthlyBudget > 0) {
            val progress = ((monthlyExpense / monthlyBudget) * 100).toInt().coerceIn(0, 100)
            progressBar.progress = progress
        } else {
            progressBar.progress = 0
        }

        // Summary Cards
        findViewById<TextView>(R.id.tvIncome).text = "+R${formatAmount(monthlyIncome)}"
        findViewById<TextView>(R.id.tvExpense).text = "-R${formatAmount(monthlyExpense)}"

        // Goal Advice
        val adviceText = findViewById<TextView>(R.id.tvGoalAdvice)
        val minGoal = AppData.getMinGoal()
        val maxGoal = AppData.getMaxGoal()
        if (maxGoal > 0) {
            when {
                monthlyExpense > maxGoal -> {
                    adviceText.text = "⚠️ You've spent R${formatAmount(monthlyExpense)}, exceeding your max goal of R${formatAmount(maxGoal)}."
                    adviceText.setTextColor(Color.parseColor("#C62828"))
                }
                monthlyExpense < minGoal -> {
                    adviceText.text = "👍 You're doing great! Monthly spending is below your min goal of R${formatAmount(minGoal)}."
                    adviceText.setTextColor(Color.parseColor("#2E7D32"))
                }
                else -> {
                    adviceText.text = "You are currently within your target spending range."
                    adviceText.setTextColor(Color.parseColor("#666666"))
                }
            }
        } else {
            adviceText.text = "Set your spending goals in the Budget tab to get advice."
            adviceText.setTextColor(Color.parseColor("#666666"))
        }

        // 4. Update the Detailed Financial Graph
        // This graph shows Monthly Input, Monthly Expense (minus), and Result
        // Alongside Total Input, Total Expense (minus), and Available Balance
        setupFinancialGraph(
            monthlyIncome, monthlyExpense, monthlyNet,
            totalIncomeAllTime, totalExpenseAllTime, availableBalance
        )

        // 5. Recent Transactions
        updateRecentTransactions(allTransactions)
    }

    private fun setupFinancialGraph(
        mIn: Double, mOut: Double, mNet: Double,
        tIn: Double, tOut: Double, tBalance: Double
    ) {
        val barChart = findViewById<BarChart>(R.id.barChart) ?: return
        
        val entries = listOf(
            BarEntry(0f, mIn.toFloat()),
            BarEntry(1f, mOut.toFloat()),
            BarEntry(2f, mNet.toFloat()),
            BarEntry(3f, tIn.toFloat()),
            BarEntry(4f, tOut.toFloat()),
            BarEntry(5f, tBalance.toFloat())
        )

        val dataSet = BarDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#81C784"), // Month Input (Light Green)
            Color.parseColor("#E57373"), // Month Exp (Light Red)
            Color.parseColor("#64B5F6"), // Month Net (Light Blue)
            Color.parseColor("#2E7D32"), // Total Input (Dark Green)
            Color.parseColor("#C62828"), // Total Exp (Dark Red)
            Color.parseColor("#1565C0")  // Total Net (Dark Blue)
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 8f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "R${formatAmount(value.toDouble())}"
            }
        }

        val data = BarData(dataSet)
        data.barWidth = 0.7f
        barChart.data = data
        
        val labels = listOf("M-In", "M-Out", "M-Net", "T-In", "T-Out", "Balance")
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            labelCount = labels.size
            textSize = 9f
        }
        
        barChart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f
        }
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun updateRecentTransactions(allTransactions: List<Transaction>) {
        val container = findViewById<LinearLayout>(R.id.transactionContainer) ?: return
        container.removeAllViews()
        val transactions = allTransactions.take(5)

        if (transactions.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No transactions yet."
            empty.setPadding(0, 16, 0, 16)
            container.addView(empty)
        } else {
            for (t in transactions) {
                val row = layoutInflater.inflate(R.layout.item_transaction_row, container, false)
                row.findViewById<TextView>(R.id.txnName).text = t.category
                row.findViewById<TextView>(R.id.txnDate).text = t.date
                val amountView = row.findViewById<TextView>(R.id.txnAmount)
                if (t.type == "expense") {
                    amountView.text = "-R${formatAmount(t.amount)}"
                    amountView.setTextColor(Color.parseColor("#C62828"))
                } else {
                    amountView.text = "+R${formatAmount(t.amount)}"
                    amountView.setTextColor(Color.parseColor("#2E7D32"))
                }
                row.setOnClickListener {
                    startActivity(Intent(this, TransactionsActivity::class.java))
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
