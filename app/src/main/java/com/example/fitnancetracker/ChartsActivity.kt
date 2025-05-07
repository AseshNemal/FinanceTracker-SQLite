package com.example.fitnancetracker


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnancetracker.model.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class ChartsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var transactionList: List<Transaction>

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)

        setupBottomNavigation()

        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        lineChart = findViewById(R.id.lineChart)
        sharedPrefs = getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)

        loadTransactions()
        showPieChart()
        showBarChart()
        showLineChart()
    }

    private fun loadTransactions() {
        val json = sharedPrefs.getString("transactions", null)
        transactionList = if (json != null) {
            val type = object : TypeToken<List<Transaction>>() {}.type
            Gson().fromJson(json, type)
        } else {
            listOf()
        }
    }

    private fun showPieChart() {
        val income = transactionList.filter { it.type == "Income" }.sumOf { it.amount.toDouble() }
        val expense = transactionList.filter { it.type == "Expense" }.sumOf { it.amount.toDouble() }

        val entries = listOf(
            PieEntry(income.toFloat(), "Income"),
            PieEntry(expense.toFloat(), "Expense")
        )

        val dataSet = PieDataSet(entries, "Income vs Expense").apply {
            colors = listOf(Color.GREEN, Color.RED)
            valueTextSize = 16f
        }

        pieChart.apply {
            data = PieData(dataSet)
            description = Description().apply { text = "Overview" }
            animateY(1000)
            invalidate()
        }
    }

    private fun showBarChart() {
        val categoryTotals = transactionList
            .filter { it.type == "Expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() } }

        val entries = categoryTotals.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        val dataSet = BarDataSet(entries, "Expenses by Category").apply {
            color = Color.BLUE
            valueTextSize = 12f
        }

        val labels = categoryTotals.keys.toList()

        barChart.apply {
            data = BarData(dataSet)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            description = Description().apply { text = "Expenses by Category" }
            animateY(1000)
            invalidate()
        }
    }

    private fun showLineChart() {
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        val groupedByDate = transactionList.groupBy {
            val date = try {
                inputDateFormat.parse(it.date)
            } catch (e: Exception) {
                null
            }
            outputDateFormat.format(date ?: Date())
        }

        val sortedDates = groupedByDate.keys.sorted()
        val entries = sortedDates.mapIndexed { index, date ->
            val totalAmount = groupedByDate[date]?.sumOf { it.amount.toDouble() } ?: 0.0
            Entry(index.toFloat(), totalAmount.toFloat())
        }

        val lineDataSet = LineDataSet(entries, "Daily Transactions").apply {
            color = Color.MAGENTA
            valueTextSize = 12f
            setDrawCircles(true)
            circleRadius = 4f
            setDrawValues(false)
        }

        lineChart.apply {
            data = LineData(lineDataSet)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(sortedDates)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -45f
            }
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            description = Description().apply { text = "Daily Transactions" }
            animateX(1000)
            invalidate()
        }
    }
}
