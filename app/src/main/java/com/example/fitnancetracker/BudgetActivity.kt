package com.example.fitnancetracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class BudgetActivity : AppCompatActivity() {

    private lateinit var etBudgetAmount: EditText
    private lateinit var btnSaveBudget: Button
    private lateinit var sharedPrefs: SharedPreferences

    private fun setupButtonListeners() {
        findViewById<Button>(R.id.btnImportExport).setOnClickListener {
            startActivity(Intent(this, ExportImportActivity::class.java))
        }

    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_Settings

        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home ->  {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_dashboard ->  {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        setupBottomNavigation()
        setupButtonListeners()

        etBudgetAmount = findViewById(R.id.etBudgetAmount)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)

        sharedPrefs = getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)



        val savedBudget = sharedPrefs.getFloat("monthly_budget", 0f)
        if (savedBudget > 0) {
            etBudgetAmount.setText(savedBudget.toString())
        }

        btnSaveBudget.setOnClickListener {
            val budgetText = etBudgetAmount.text.toString().trim()
            val budgetAmount = budgetText.toFloatOrNull()

            if (budgetAmount == null || budgetAmount <= 0) {
                Toast.makeText(this, "Enter a valid budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedPrefs.edit().putFloat("monthly_budget", budgetAmount).apply()
            Toast.makeText(this, "Budget Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


}
