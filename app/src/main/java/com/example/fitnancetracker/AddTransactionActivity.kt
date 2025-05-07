package com.example.fitnancetracker

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnancetracker.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var spCategory: Spinner
    private lateinit var rgType: RadioGroup
    private lateinit var btnPickDate: Button
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSave: Button
    private lateinit var sharedPrefs: SharedPreferences

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
        sharedPrefs = getSharedPreferences("FinancePrefs", MODE_PRIVATE)

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

        val existingListJson = sharedPrefs.getString("transactions", null)
        val type = object : TypeToken<MutableList<Transaction>>() {}.type
        val transactionList: MutableList<Transaction> = if (existingListJson != null) {
            Gson().fromJson(existingListJson, type)
        } else {
            mutableListOf()
        }

        transactionList.add(newTransaction)

        val updatedJson = Gson().toJson(transactionList)
        val editor = sharedPrefs.edit()
        editor.putString("transactions", updatedJson)

        if (isIncome) {
            val currentIncome = sharedPrefs.getFloat("total_income", 0f)
            editor.putFloat("total_income", currentIncome + amount)
        } else {
            val currentExpense = sharedPrefs.getFloat("total_expense", 0f)
            editor.putFloat("total_expense", currentExpense + amount)
        }

        editor.apply()

        Toast.makeText(this, "Transaction Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
