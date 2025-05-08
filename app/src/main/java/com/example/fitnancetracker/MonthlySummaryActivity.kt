package com.example.fitnancetracker

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnancetracker.adapter.CategorySummaryAdapter
import com.example.fitnancetracker.model.CategorySummary
import com.example.fitnancetracker.model.Transaction
import com.example.fitnancetracker.viewmodel.TransactionViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class MonthlySummaryActivity : AppCompatActivity() {
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvSavings: TextView
    private lateinit var tvBudgetUsage: TextView
    private lateinit var rvCategorySummary: RecyclerView
    private lateinit var pieChart: PieChart
    private lateinit var bottomNavigation: BottomNavigationView
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_summary)

        try {
            initializeViews()
            setupRecyclerView()
            setupPieChart()
            setupBottomNavigation()
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
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH) + 1 // Adding 1 because Calendar.MONTH is 0-based
                val currentYear = calendar.get(Calendar.YEAR)

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
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        startActivity(android.content.Intent(this, MainActivity::class.java))
                        true
                    }
                    R.id.nav_dashboard -> {
                        startActivity(android.content.Intent(this, MonthlySummaryActivity::class.java))
                        true
                    }
                    R.id.nav_notifications -> {
                        startActivity(android.content.Intent(this, NotificationsActivity::class.java))
                        true
                    }
                    R.id.nav_Settings -> {
                        startActivity(android.content.Intent(this, BudgetActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            bottomNavigation.selectedItemId = R.id.nav_dashboard
        } catch (e: Exception) {
            Log.e("MonthlySummary", "Error setting up bottom navigation: ${e.message}", e)
        }
    }
}
