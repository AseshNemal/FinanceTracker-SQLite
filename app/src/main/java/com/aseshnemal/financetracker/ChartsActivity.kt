package com.aseshnemal.financetracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aseshnemal.financetracker.model.Transaction
import com.aseshnemal.financetracker.viewmodel.TransactionViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChartsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private val viewModel: TransactionViewModel by viewModels()

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home ->  {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_dashboard ->  {
                    startActivity(Intent(this, MonthlySummaryActivity::class.java))
                    finish()
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)

        setupBottomNavigation()

        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        lineChart = findViewById(R.id.lineChart)

        setupCharts()
        loadAndDisplayData()
    }

    private fun setupCharts() {
        // Setup Pie Chart
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            legend.isEnabled = true
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }

        // Setup Bar Chart
        barChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setScaleEnabled(true)
            setPinchZoom(false)
            setDrawValueAboveBar(true)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
        }

        // Setup Line Chart
        lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setDrawBorders(false)
            setScaleEnabled(true)
            setPinchZoom(false)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -45f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
        }
    }

    private fun loadAndDisplayData() {
        lifecycleScope.launch {
            try {
                val transactions = viewModel.allTransactions.first()
                showPieChart(transactions)
                showBarChart(transactions)
                showLineChart(transactions)
            } catch (e: Exception) {
                Log.e("ChartsActivity", "Error loading data: ${e.message}", e)
            }
        }
    }

    private fun showPieChart(transactions: List<Transaction>) {
        try {
            val income = transactions.filter { it.type == "Income" }.sumOf { it.amount.toDouble() }
            val expense = transactions.filter { it.type == "Expense" }.sumOf { it.amount.toDouble() }

            val entries = listOf(
                PieEntry(income.toFloat(), "Income"),
                PieEntry(expense.toFloat(), "Expense")
            )

            val dataSet = PieDataSet(entries, "Income vs Expense").apply {
                colors = listOf(Color.GREEN, Color.RED)
                valueTextSize = 16f
                valueTextColor = Color.WHITE
            }

            pieChart.apply {
                data = PieData(dataSet)
                animateY(1000)
                invalidate()
            }
        } catch (e: Exception) {
            Log.e("ChartsActivity", "Error showing pie chart: ${e.message}", e)
        }
    }

    private fun showBarChart(transactions: List<Transaction>) {
        try {
            val categoryTotals = transactions
                .filter { it.type == "Expense" }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() } }

            val entries = categoryTotals.entries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.toFloat())
            }

            val dataSet = BarDataSet(entries, "Expenses by Category").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 12f
                valueTextColor = Color.BLACK
            }

            val labels = categoryTotals.keys.toList()

            barChart.apply {
                data = BarData(dataSet)
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                animateY(1000)
                invalidate()
            }
        } catch (e: Exception) {
            Log.e("ChartsActivity", "Error showing bar chart: ${e.message}", e)
        }
    }

    private fun showLineChart(transactions: List<Transaction>) {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

            val groupedByDate = transactions.groupBy {
                try {
                    val date = dateFormat.parse(it.date)
                    outputDateFormat.format(date ?: Date())
                } catch (e: Exception) {
                    Log.e("ChartsActivity", "Error parsing date: ${it.date}", e)
                    outputDateFormat.format(Date())
                }
            }

            val sortedDates = groupedByDate.keys.sorted()
            val entries = sortedDates.mapIndexed { index, date ->
                val totalAmount = groupedByDate[date]?.sumOf { it.amount.toDouble() } ?: 0.0
                Entry(index.toFloat(), totalAmount.toFloat())
            }

            val lineDataSet = LineDataSet(entries, "Daily Transactions").apply {
                color = Color.BLUE
                valueTextSize = 12f
                setDrawCircles(true)
                circleRadius = 4f
                setDrawValues(false)
                lineWidth = 2f
            }

            lineChart.apply {
                data = LineData(lineDataSet)
                xAxis.valueFormatter = IndexAxisValueFormatter(sortedDates)
                animateX(1000)
                invalidate()
            }
        } catch (e: Exception) {
            Log.e("ChartsActivity", "Error showing line chart: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayData()
    }
}
