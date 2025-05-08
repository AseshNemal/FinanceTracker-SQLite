package com.example.fitnancetracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.fitnancetracker.utils.NotificationHelper
import com.example.fitnancetracker.viewmodel.TransactionViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvSavings: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var monthlySummaryContainer: LinearLayout
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNavigation()

        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        tvSavings = findViewById(R.id.tvSavings)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        budgetProgressBar = findViewById(R.id.budgetProgressBar)
        monthlySummaryContainer = findViewById(R.id.monthlySummaryContainer)

        updateDashboard()
        setupButtonListeners()
        setupBottomNavigation()
        requestNotificationPermission()
        checkBudgetExceeded()
        setupMonthlySummary()
    }

    private fun updateDashboard() {
        lifecycleScope.launch {
            val incomeTransactions = viewModel.getTransactionsByType("Income").first()
            val expenseTransactions = viewModel.getTransactionsByType("Expense").first()
            val budget = viewModel.getMonthlyBudget().first() ?: 1f // avoid division by zero
            
            val income = incomeTransactions.sumOf { it.amount.toDouble() }.toFloat()
            val expense = expenseTransactions.sumOf { it.amount.toDouble() }.toFloat()
            val savings = income - expense
            val usagePercent = ((expense / budget) * 100).toInt().coerceAtMost(100)

            tvIncome.text = "Total Income: Rs. %.2f".format(income)
            tvExpense.text = "Total Expenses: Rs. %.2f".format(expense)
            tvSavings.text = "Savings: Rs. %.2f".format(savings)
            tvBudgetStatus.text = "Budget Usage: $usagePercent%"
            budgetProgressBar.progress = usagePercent
        }
    }

    private fun setupMonthlySummary() {
        lifecycleScope.launch {
            monthlySummaryContainer.removeAllViews()

            // Get current month and year
            val calendar = Calendar.getInstance()
            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val currentMonth = monthFormat.format(calendar.time)

            // Get transactions for current month
            val currentMonthNum = calendar.get(Calendar.MONTH) + 1 // Adding 1 because Calendar.MONTH is 0-based
            val currentYear = calendar.get(Calendar.YEAR)
            val monthYear = "$currentMonthNum/$currentYear"
            
            val transactions = viewModel.allTransactions.first()
            val monthlyTransactions = transactions.filter { transaction ->
                try {
                    val parts = transaction.date.split("/")
                    if (parts.size == 3) {
                        val transactionMonth = parts[1].toInt()
                        val transactionYear = parts[2].toInt()
                        transactionMonth == currentMonthNum && transactionYear == currentYear
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
            
            val income = monthlyTransactions.filter { it.type == "Income" }.sumOf { it.amount.toDouble() }.toFloat()
            val expense = monthlyTransactions.filter { it.type == "Expense" }.sumOf { it.amount.toDouble() }.toFloat()
            val savings = income - expense
            val budget = viewModel.getMonthlyBudget().first() ?: 0f
            val usagePercent = if (budget > 0) ((expense / budget) * 100).toInt() else 0

            // Create summary card
            val summaryCard = CardView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                radius = 16f
                cardElevation = 4f
                setContentPadding(16, 16, 16, 16)
            }

            val summaryLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Add title
            val title = TextView(this@MainActivity).apply {
                text = "Monthly Summary ($currentMonth)"
                textSize = 18f
                setTextColor(resources.getColor(R.color.primaryDarkBlue, theme))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 12)
                }
            }

            val incomeItem = createSummaryItem("Income", "Rs. %.2f".format(income), R.color.incomeGreen)
            val expenseItem = createSummaryItem("Expenses", "Rs. %.2f".format(expense), R.color.expenseRed)
            val savingsItem = createSummaryItem("Savings", "Rs. %.2f".format(savings),
                if (savings >= 0) R.color.savingsBlue else R.color.expenseRed)
            val budgetItem = createSummaryItem("Budget Usage", "$usagePercent%", R.color.primaryDarkBlue)

            summaryLayout.addView(title)
            summaryLayout.addView(incomeItem)
            summaryLayout.addView(expenseItem)
            summaryLayout.addView(savingsItem)
            summaryLayout.addView(budgetItem)

            summaryCard.addView(summaryLayout)
            monthlySummaryContainer.addView(summaryCard)
        }
    }

    private fun createSummaryItem(label: String, value: String, colorRes: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }

            val labelView = TextView(context).apply {
                text = "$label:"
                textSize = 16f
                setTextColor(resources.getColor(R.color.textSecondary, theme))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val valueView = TextView(context).apply {
                text = value
                textSize = 16f
                setTextColor(resources.getColor(colorRes, theme))
            }

            addView(labelView)
            addView(valueView)
        }
    }

    private fun setupButtonListeners() {
        findViewById<Button>(R.id.btnAddTransaction).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewTransactions).setOnClickListener {
            startActivity(Intent(this, ViewTransactionsActivity::class.java))
        }

        findViewById<Button>(R.id.btnBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        findViewById<Button>(R.id.btnCharts).setOnClickListener {
            startActivity(Intent(this, ChartsActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {


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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    private fun checkBudgetExceeded() {
        lifecycleScope.launch {
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
            
            if (budget > 0 && monthlyExpenses > budget) {
                NotificationHelper.showNotification(
                    this@MainActivity,
                    "Budget Alert!",
                    "You've exceeded your monthly budget by Rs. %.2f".format(monthlyExpenses - budget)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
        setupMonthlySummary()
    }
}