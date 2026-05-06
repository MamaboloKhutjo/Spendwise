package com.example.spendwise

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddBudgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val spinner = findViewById<Spinner>(R.id.spinnerBudgetCategory)
        val etLimit = findViewById<EditText>(R.id.etBudgetLimit)
        val btnSave = findViewById<Button>(R.id.btnSaveBudget)

        btnBack.setOnClickListener { finish() }

        val categories = AppData.getCategories()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Pre-fill existing limit if set
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                val category = categories[pos]
                val existing = AppData.getBudgetLimit(category)
                if (existing > 0) {
                    etLimit.setText(existing.toString())
                } else {
                    etLimit.text.clear()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnSave.setOnClickListener {
            val limitStr = etLimit.text.toString().trim()
            if (limitStr.isEmpty() || limitStr.toDoubleOrNull() == null) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val limit = limitStr.toDouble()
            val category = spinner.selectedItem.toString()
            AppData.setBudget(category, limit)
            Toast.makeText(this, "Budget saved for $category!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, BudgetActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
