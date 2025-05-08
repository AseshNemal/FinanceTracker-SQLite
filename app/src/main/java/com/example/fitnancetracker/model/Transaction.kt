package com.example.fitnancetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String,
    val title: String,
    val amount: Float,
    val category: String,
    val date: String,
    val type: String = "General"
)