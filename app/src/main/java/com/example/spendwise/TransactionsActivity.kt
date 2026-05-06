package com.example.spendwise

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TransactionsActivity : AppCompatActivity() {

    private var showingAll = false
    private var startDate: Calendar = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    private var endDate: Calendar = Calendar.getInstance()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displaySdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        setupBottomNav()

        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)

        updateDateLabels()

        tvStartDate.setOnClickListener { showDatePicker(startDate, true) }
        tvEndDate.setOnClickListener { showDatePicker(endDate, false) }

        findViewById<Button>(R.id.btnViewMore).setOnClickListener {
            showingAll = !showingAll
            refreshList()
            val btn = findViewById<Button>(R.id.btnViewMore)
            btn.text = if (showingAll) "Show Less" else "View More"
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun showDatePicker(calendar: Calendar, isStart: Boolean) {
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            updateDateLabels()
            refreshList()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateLabels() {
        tvStartDate.text = displaySdf.format(startDate.time)
        tvEndDate.text = displaySdf.format(endDate.time)
    }

    private fun refreshList() {
        val startStr = sdf.format(startDate.time)
        val endStr = sdf.format(endDate.time)
        val all = AppData.getTransactionsInPeriod(startStr, endStr)
        
        val toShow = if (showingAll) all else all.take(10)
        val container = findViewById<LinearLayout>(R.id.transactionContainer)
        container.removeAllViews()

        findViewById<Button>(R.id.btnViewMore).visibility =
            if (all.size > 10) View.VISIBLE else View.GONE

        if (toShow.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No transactions found for this period."
            empty.setTextColor(Color.parseColor("#888888"))
            empty.setPadding(32, 24, 32, 24)
            empty.gravity = android.view.Gravity.CENTER
            container.addView(empty)
            return
        }

        for (t in toShow) {
            val row = layoutInflater.inflate(R.layout.item_transaction, container, false)
            row.findViewById<TextView>(R.id.txnName).text = t.category
            row.findViewById<TextView>(R.id.txnDate).text = t.date
            
            val timeView = row.findViewById<TextView>(R.id.txnTime)
            if (t.startTime != null && t.endTime != null) {
                timeView.text = "${t.startTime} - ${t.endTime}"
                timeView.visibility = View.VISIBLE
            } else {
                timeView.visibility = View.GONE
            }

            val noteView = row.findViewById<TextView>(R.id.txnNote)
            if (t.note.isNotEmpty()) {
                noteView.text = t.note
                noteView.visibility = View.VISIBLE
            } else {
                noteView.visibility = View.GONE
            }

            val imageView = row.findViewById<ImageView>(R.id.txnImage)
            if (t.imagePath != null) {
                val imgFile = File(t.imagePath)
                if (imgFile.exists()) {
                    imageView.setImageURI(Uri.fromFile(imgFile))
                    imageView.visibility = View.VISIBLE
                } else {
                    imageView.visibility = View.GONE
                }
            } else {
                imageView.visibility = View.GONE
            }

            val amountView = row.findViewById<TextView>(R.id.txnAmount)
            if (t.type == "expense") {
                amountView.text = "-R${formatAmount(t.amount)}"
                amountView.setTextColor(Color.parseColor("#C62828"))
            } else {
                amountView.text = "+R${formatAmount(t.amount)}"
                amountView.setTextColor(Color.parseColor("#2E7D32"))
            }

            row.setOnClickListener { showTransactionDetail(t) }

            container.addView(row)
        }
    }

    private fun showTransactionDetail(t: Transaction) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_detail, null)
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        builder.setView(dialogView)
        val dialog = builder.create()

        dialogView.findViewById<TextView>(R.id.detailCategory).text = t.category
        val amountView = dialogView.findViewById<TextView>(R.id.detailAmount)
        if (t.type == "expense") {
            amountView.text = "-R${formatAmount(t.amount)}"
            amountView.setTextColor(Color.parseColor("#C62828"))
        } else {
            amountView.text = "+R${formatAmount(t.amount)}"
            amountView.setTextColor(Color.parseColor("#2E7D32"))
        }

        dialogView.findViewById<TextView>(R.id.detailDate).text = "Date: ${t.date}"
        if (t.startTime != null && t.endTime != null) {
            dialogView.findViewById<TextView>(R.id.detailTime).text = "Time: ${t.startTime} - ${t.endTime}"
        } else {
            dialogView.findViewById<TextView>(R.id.detailTime).visibility = View.GONE
        }

        val noteView = dialogView.findViewById<TextView>(R.id.detailNote)
        if (t.note.isNotEmpty()) {
            noteView.text = t.note
        } else {
            noteView.visibility = View.GONE
        }

        val imageView = dialogView.findViewById<ImageView>(R.id.detailImage)
        if (t.imagePath != null) {
            val file = File(t.imagePath)
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file))
                imageView.visibility = View.VISIBLE
            }
        }

        dialogView.findViewById<Button>(R.id.btnCloseDetail).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
        findViewById<LinearLayout>(R.id.navTransactions).setOnClickListener { }
        findViewById<LinearLayout>(R.id.navBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
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
