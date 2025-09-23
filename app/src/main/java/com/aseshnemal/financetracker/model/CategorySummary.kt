package com.aseshnemal.financetracker.model

data class CategorySummary(
    val category: String,
    val totalAmount: Float,
    val percentage: Int = 0,
    val isIncome: Boolean = false
)