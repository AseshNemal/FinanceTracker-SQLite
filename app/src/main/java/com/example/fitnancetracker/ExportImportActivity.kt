package com.example.fitnancetracker

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnancetracker.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.nio.charset.StandardCharsets

class ExportImportActivity : AppCompatActivity() {

    private lateinit var btnExport: Button
    private lateinit var btnImport: Button
    private lateinit var sharedPrefs: SharedPreferences
    private val gson = Gson()

    private val EXPORT_REQUEST_CODE = 1001
    private val IMPORT_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_import)

        btnExport = findViewById(R.id.btnExport)
        btnImport = findViewById(R.id.btnImport)
        sharedPrefs = getSharedPreferences("FinancePrefs", MODE_PRIVATE)

        btnExport.setOnClickListener { exportData() }
        btnImport.setOnClickListener { importData() }

        setupBottomNavigation()
    }

    private fun exportData() {
        val transactionsJson = sharedPrefs.getString("transactions", null)
        if (transactionsJson.isNullOrEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/txt"
            putExtra(Intent.EXTRA_TITLE, "transactions_backup.txt")
        }
        startActivityForResult(intent, EXPORT_REQUEST_CODE)
    }

    private fun importData() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/txt"
        }
        startActivityForResult(intent, IMPORT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return

        val uri = data.data ?: return

        when (requestCode) {
            EXPORT_REQUEST_CODE -> {
                val transactionsJson = sharedPrefs.getString("transactions", null)
                try {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(transactionsJson?.toByteArray(StandardCharsets.UTF_8))
                        Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show()
                }
            }

            IMPORT_REQUEST_CODE -> {
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        val importedJson = reader.readText()

                        val type = object : TypeToken<MutableList<Transaction>>() {}.type
                        val importedList: MutableList<Transaction> = gson.fromJson(importedJson, type)

                        val existingJson = sharedPrefs.getString("transactions", null)
                        val existingList: MutableList<Transaction> = if (existingJson != null) {
                            gson.fromJson(existingJson, type)
                        } else {
                            mutableListOf()
                        }

                        existingList.addAll(importedList)
                        val updatedJson = gson.toJson(existingList)

                        sharedPrefs.edit().putString("transactions", updatedJson).apply()
                        Toast.makeText(this, "Data imported successfully", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to import data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_Settings

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener { item ->
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
