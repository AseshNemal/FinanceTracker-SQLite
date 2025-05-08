package com.example.fitnancetracker.model

import androidx.room.Entity

@Entity
data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val type: String = "General"
)