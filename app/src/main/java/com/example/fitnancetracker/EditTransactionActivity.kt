package com.example.fitnancetracker

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitnancetracker.model.Transaction
import com.example.fitnancetracker.viewmodel.TransactionViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var rgType: RadioGroup
    private lateinit var btnSave: Button
    private val viewModel: TransactionViewModel by viewModels()

    private var transactionId: String = ""
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

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions)
        spinnerCategory.adapter = adapter

        transactionId = intent.getStringExtra("transaction_id") ?: ""
        loadTransaction()

        btnSave.setOnClickListener {
            updateTransaction()
        }
    }

    private fun loadTransaction() {
        lifecycleScope.launch {
            val transactions = viewModel.allTransactions.first()
            val transaction = transactions.find { it.id == transactionId }
            transaction?.let {
                etTitle.setText(it.title)
                etAmount.setText(it.amount.toString())

                val categoryIndex = categoryOptions.indexOf(it.category)
                if (categoryIndex >= 0) {
                    spinnerCategory.setSelection(categoryIndex)
                }

                if (it.type == "Income") {
                    rgType.check(R.id.rbIncome)
                } else {
                    rgType.check(R.id.rbExpense)
                }
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

        lifecycleScope.launch {
            val transactions = viewModel.allTransactions.first()
            val original = transactions.find { it.id == transactionId }
            original?.let {
                val updatedTransaction = it.copy(
                    title = title,
                    amount = amount,
                    category = category,
                    type = type
                )
                viewModel.update(updatedTransaction)
                Toast.makeText(this@EditTransactionActivity, "Transaction Updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
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
