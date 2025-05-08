package com.example.fitnancetracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget")
data class Budget(
    @PrimaryKey
    val id: Int = 1, // We'll only have one budget record
    val amount: Float
) 