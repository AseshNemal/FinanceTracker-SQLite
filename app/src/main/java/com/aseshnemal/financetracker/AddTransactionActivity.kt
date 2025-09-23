package com.aseshnemal.financetracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aseshnemal.financetracker.model.Transaction
import com.aseshnemal.financetracker.utils.NotificationHelper
import com.aseshnemal.financetracker.viewmodel.TransactionViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var spCategory: Spinner
    private lateinit var rgType: RadioGroup
    private lateinit var btnPickDate: Button
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSave: Button
    private val viewModel: TransactionViewModel by viewModels()

    private var selectedDate: String = ""
    private var selectedCategory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        etTitle = findViewById(R.id.etTitle)
        etAmount = findViewById(R.id.etAmount)
        spCategory = findViewById(R.id.spinnerCategory)
        rgType = findViewById(R.id.rgType)
        btnPickDate = findViewById(R.id.btnPickDate)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSave = findViewById(R.id.btnSave)

        setupCategorySpinner()
        setupBottomNavigation()

        btnPickDate.setOnClickListener {
            showDatePicker()
        }

        btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Food", "Transport", "Shopping", "Health", "Salary", "Entertainment", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = adapter

        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                selectedCategory = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = ""
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = "$day/${month + 1}/$year"
                tvSelectedDate.text = "Selected Date: $selectedDate"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
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

    private fun saveTransaction() {
        val title = etTitle.text.toString().trim()
        val amountText = etAmount.text.toString().trim()
        val category = selectedCategory
        val isIncome = rgType.checkedRadioButtonId == R.id.rbIncome

        if (title.isEmpty() || amountText.isEmpty() || category.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toFloatOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val transactionType = if (isIncome) "Income" else "Expense"
        val newTransaction = Transaction(
            UUID.randomUUID().toString(),
            title,
            amount,
            category,
            selectedDate,
            transactionType
        )

        lifecycleScope.launch {
            viewModel.insert(newTransaction)
            
            // If it's an expense, check budget status
            if (!isIncome) {
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                
                val monthlyExpenses = viewModel.getTransactionsByType("Expense").first()
                    .filter { transaction ->
                        try {
                            val parts = transaction.date.split("/")
                            if (parts.size == 3) {
                                val transactionMonth = parts[1].toInt()
                                val transactionYear = parts[2].toInt()
                                transactionMonth == currentMonth && transactionYear == currentYear
                            } else false
                        } catch (e: Exception) {
                            false
                        }
                    }
                    .sumOf { it.amount.toDouble() }.toFloat()
                
                val budget = viewModel.getMonthlyBudget().first() ?: 0f
                
                if (budget > 0) {
                    val usagePercent = ((monthlyExpenses / budget) * 100).toInt()
                    when {
                        monthlyExpenses > budget -> {
                            NotificationHelper.showNotification(
                                this@AddTransactionActivity,
                                "Budget Exceeded!",
                                "You've exceeded your monthly budget by Rs. %.2f".format(monthlyExpenses - budget)
                            )
                        }
                        usagePercent >= 80 -> {
                            NotificationHelper.showNotification(
                                this@AddTransactionActivity,
                                "Budget Warning",
                                "You've reached ${usagePercent}% of your monthly budget"
                            )
                        }
                    }
                }
            }
            
            Toast.makeText(this@AddTransactionActivity, "Transaction Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
