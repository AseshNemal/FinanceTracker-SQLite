package com.example.fitnancetracker.model

data class Transaction(
    val id: String,
    val title: String,
    val amount: Float,
    val category: String,
    val date: String,
    val type: String = "General"
)