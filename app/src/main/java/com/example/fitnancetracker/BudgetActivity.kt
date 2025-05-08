package com.example.fitnancetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitnancetracker.viewmodel.TransactionViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BudgetActivity : AppCompatActivity() {

    private lateinit var etBudgetAmount: EditText
    private lateinit var btnSaveBudget: Button
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        etBudgetAmount = findViewById(R.id.etBudgetAmount)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)

        setupButtonListeners()
        setupBottomNavigation()
        loadCurrentBudget()
    }

    private fun loadCurrentBudget() {
        lifecycleScope.launch {
            val currentBudget = viewModel.getMonthlyBudget().first()
            etBudgetAmount.setText(currentBudget.toString())
        }
    }

    private fun setupButtonListeners() {
        btnSaveBudget.setOnClickListener {
            val budgetText = etBudgetAmount.text.toString()
            if (budgetText.isNotEmpty()) {
                val budgetAmount = budgetText.toFloatOrNull()
                if (budgetAmount != null && budgetAmount > 0) {
                    lifecycleScope.launch {
                        viewModel.setMonthlyBudget(budgetAmount)
                        Toast.makeText(this@BudgetActivity, "Budget saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnImportExport).setOnClickListener {
            startActivity(Intent(this, ExportImportActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_Settings

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
