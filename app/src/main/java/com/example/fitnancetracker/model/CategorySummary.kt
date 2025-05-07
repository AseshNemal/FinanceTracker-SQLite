package com.example.fitnancetracker.model

data class CategorySummary(
    val category: String,
    val totalAmount: Double,
    val percentage: Int,  // Add this property
    val isIncome: Boolean = false
)