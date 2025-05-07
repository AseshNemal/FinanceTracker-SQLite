package com.example.fitnancetracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnancetracker.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var rgType: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var sharedPrefs: SharedPreferences

    private var transactionIndex: Int = -1
    private var transactions: MutableList<Transaction> = mutableListOf()

    private val categoryOptions = arrayOf("Food", "Transport", "Shopping", "Salary", "Entertainment", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        setupBottomNavigation()

        etTitle = findViewById(R.id.editTitle)
        etAmount = findViewById(R.id.editAmount)
        spinnerCategory = findViewById(R.id.editSpinnerCategory)
        rgType = findViewById(R.id.editType)
        btnSave = findViewById(R.id.btnSaveChanges)
        sharedPrefs = getSharedPreferences("FinancePrefs", MODE_PRIVATE)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions)
        spinnerCategory.adapter = adapter

        transactionIndex = intent.getIntExtra("transaction_index", -1)
        loadTransaction()

        btnSave.setOnClickListener {
            updateTransaction()
        }
    }

    private fun loadTransaction() {
        val json = sharedPrefs.getString("transactions", null)
        val type = object : TypeToken<MutableList<Transaction>>() {}.type
        transactions = if (json != null) Gson().fromJson(json, type) else mutableListOf()

        if (transactionIndex != -1) {
            val txn = transactions[transactionIndex]
            etTitle.setText(txn.title)
            etAmount.setText(txn.amount.toString())

            val categoryIndex = categoryOptions.indexOf(txn.category)
            if (categoryIndex >= 0) {
                spinnerCategory.setSelection(categoryIndex)
            }

            if (txn.type == "Income") {
                rgType.check(R.id.rbIncome)
            } else {
                rgType.check(R.id.rbExpense)
            }
        }
    }

    private fun updateTransaction() {
        val title = etTitle.text.toString()
        val amount = etAmount.text.toString().toFloatOrNull() ?: 0f
        val category = spinnerCategory.selectedItem.toString()
        val type = if (rgType.checkedRadioButtonId == R.id.rbIncome) "Income" else "Expense"

        if (title.isEmpty() || amount <= 0 || category.isEmpty()) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
            return
        }

        val original = transactions[transactionIndex]
        transactions[transactionIndex] = original.copy(
            title = title,
            amount = amount,
            category = category,
            type = type
        )

        sharedPrefs.edit().putString("transactions", Gson().toJson(transactions)).apply()
        Toast.makeText(this, "Transaction Updated!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, MonthlySummaryActivity::class.java))
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.nav_Settings -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
