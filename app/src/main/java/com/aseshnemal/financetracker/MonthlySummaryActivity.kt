package com.aseshnemal.financetracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aseshnemal.financetracker.adapter.CategorySummaryAdapter
import com.aseshnemal.financetracker.model.CategorySummary
import com.aseshnemal.financetracker.model.Transaction
import com.aseshnemal.financetracker.viewmodel.TransactionViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MonthlySummaryActivity : AppCompatActivity() {
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvSavings: TextView
    private lateinit var tvBudgetUsage: TextView
    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnPreviousMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var rvCategorySummary: RecyclerView
    private lateinit var pieChart: PieChart
    private lateinit var bottomNavigation: BottomNavigationView
    private val viewModel: TransactionViewModel by viewModels()

    private var selectedMonth: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_summary)

        try {
            initializeViews()
            setupRecyclerView()
            setupPieChart()
            setupBottomNavigation()
            setupMonthNavigation()
            loadMonthlyData()
        } catch (e: Exception) {
            Log.e("MonthlySummary", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error initializing the screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            tvTotalIncome = findViewById(R.id.tvTotalIncome)
            tvTotalExpense = findViewById(R.id.tvTotalExpense)
            tvSavings = findViewById(R.id.tvSavings)
            tvBudgetUsage = findViewById(R.id.tvBudgetUsage)
            tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
            btnPreviousMonth = findViewById(R.id.btnPreviousMonth)
            btnNextMonth = findViewById(R.id.btnNextMonth)
            rvCategorySummary = findViewById(R.id.rvCategorySummary)
            pieChart = findViewById(R.id.pieChart)
            bottomNavigation = findViewById(R.id.bottom_navigation)

            // Set initial text
            tvTotalIncome.text = "Total Income: Rs. 0.00"
            tvTotalExpense.text = "Total Expenses: Rs. 0.00"
            tvSavings.text = "Savings: Rs. 0.00"
            tvBudgetUsage.text = "Budget Usage: 0%"
        } catch (e: Exception) {
            Log.e("MonthlySummary", "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupMonthNavigation() {
        btnPreviousMonth.setOnClickListener {
            selectedMonth.add(Calendar.MONTH, -1)
            updateMonthDisplay()
            loadMonthlyData()
        }

        btnNextMonth.setOnClickListener {
            selectedMonth.add(Calendar.MONTH, 1)
            updateMonthDisplay()
            loadMonthlyData()
        }

        updateMonthDisplay()
    }

    private fun updateMonthDisplay() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvCurrentMonth.text = monthFormat.format(selectedMonth.time)
    }

    private fun setupRecyclerView() {
        rvCategorySummary.layoutManager = LinearLayoutManager(this)
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleRadius(61f)
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            legend.isEnabled = true
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }
    }

    private fun loadMonthlyData() {
        lifecycleScope.launch {
            try {
                val currentMonth = selectedMonth.get(Calendar.MONTH) + 1 // Adding 1 because Calendar.MONTH is 0-based
                val currentYear = selectedMonth.get(Calendar.YEAR)

                Log.d("MonthlySummary", "Loading data for month: $currentMonth, year: $currentYear")

                val transactions = viewModel.allTransactions.first()
                Log.d("MonthlySummary", "Total transactions loaded: ${transactions.size}")

                val monthlyTransactions = transactions.filter { transaction ->
                    try {
                        val parts = transaction.date.split("/")
                        if (parts.size == 3) {
                            val transactionMonth = parts[1].toInt()
                            val transactionYear = parts[2].toInt()
                            val isCurrentMonth = transactionMonth == currentMonth && transactionYear == currentYear
                            Log.d("MonthlySummary", "Transaction date: ${transaction.date}, isCurrentMonth: $isCurrentMonth")
                            isCurrentMonth
                        } else {
                            Log.e("MonthlySummary", "Invalid date format: ${transaction.date}")
                            false
                        }
                    } catch (e: Exception) {
                        Log.e("MonthlySummary", "Error parsing date: ${transaction.date}", e)
                        false
                    }
                }

                Log.d("MonthlySummary", "Monthly transactions filtered: ${monthlyTransactions.size}")

                val totalIncome = monthlyTransactions.filter { it.type == "Income" }.sumOf { it.amount.toDouble() }
                val totalExpense = monthlyTransactions.filter { it.type == "Expense" }.sumOf { it.amount.toDouble() }
                val savings = totalIncome - totalExpense

                Log.d("MonthlySummary", "Total Income: $totalIncome")
                Log.d("MonthlySummary", "Total Expense: $totalExpense")
                Log.d("MonthlySummary", "Savings: $savings")

                tvTotalIncome.text = "Total Income: Rs. %.2f".format(totalIncome)
                tvTotalExpense.text = "Total Expenses: Rs. %.2f".format(totalExpense)
                tvSavings.text = "Savings: Rs. %.2f".format(savings)

                // Calculate category-wise summaries
                val categorySummaries = calculateCategorySummaries(monthlyTransactions)
                Log.d("MonthlySummary", "Category summaries calculated: ${categorySummaries.size}")
                
                rvCategorySummary.adapter = CategorySummaryAdapter(categorySummaries)

                // Update pie chart
                updatePieChart(categorySummaries)

                // Update budget usage
                val budget = viewModel.getMonthlyBudget().first() ?: 0f
                val usagePercent = if (budget > 0) ((totalExpense / budget) * 100).toInt() else 0
                tvBudgetUsage.text = "Budget Usage: $usagePercent%"
            } catch (e: Exception) {
                Log.e("MonthlySummary", "Error loading monthly data: ${e.message}", e)
                Toast.makeText(this@MonthlySummaryActivity, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateCategorySummaries(transactions: List<Transaction>): List<CategorySummary> {
        try {
            val categoryMap = mutableMapOf<String, Float>()
            val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount.toDouble() }.toFloat()

            Log.d("MonthlySummary", "Calculating summaries for ${transactions.size} transactions")
            Log.d("MonthlySummary", "Total expense: $totalExpense")

            transactions.filter { it.type == "Expense" }.forEach { transaction ->
                val currentAmount = categoryMap[transaction.category] ?: 0f
                categoryMap[transaction.category] = currentAmount + transaction.amount
                Log.d("MonthlySummary", "Category: ${transaction.category}, Amount: ${transaction.amount}")
            }

            return categoryMap.map { (category, amount) ->
                val percentage = if (totalExpense > 0) ((amount / totalExpense) * 100).toInt() else 0
                Log.d("MonthlySummary", "Category: $category, Total: $amount, Percentage: $percentage%")
                CategorySummary(category, amount, percentage)
            }.sortedByDescending { it.totalAmount }
        } catch (e: Exception) {
            Log.e("MonthlySummary", "Error calculating category summaries: ${e.message}", e)
            return emptyList()
        }
    }

    private fun updatePieChart(summaries: List<CategorySummary>) {
        try {
            if (summaries.isEmpty()) {
                Log.d("MonthlySummary", "No data to display in pie chart")
                pieChart.clear()
                pieChart.invalidate()
                return
            }

            val entries = summaries.map { 
                PieEntry(it.totalAmount, it.category)
            }

            Log.d("MonthlySummary", "Creating pie chart with ${entries.size} entries")

            val dataSet = PieDataSet(entries, "Expenses by Category").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 12f
                valueTextColor = Color.WHITE
            }

            pieChart.data = PieData(dataSet)
            pieChart.invalidate()
            Log.d("MonthlySummary", "Pie chart updated successfully")
        } catch (e: Exception) {
            Log.e("MonthlySummary", "Error updating pie chart: ${e.message}", e)
        }
    }

    private fun setupBottomNavigation() {
        try {
            bottomNavigation.selectedItemId = R.id.nav_dashboard
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_dashboard -> {
                        true
                    }
                    R.id.nav_notifications -> {
                        startActivity(Intent(this, NotificationsActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_Settings -> {
                        startActivity(Intent(this, BudgetActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e("MonthlySummary", "Error setting up bottom navigation: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        loadMonthlyData()
    }
}
