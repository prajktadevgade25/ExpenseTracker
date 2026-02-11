package com.example.expensetracker.data.dao

data class TransactionWithCategory(
    val id: Int,
    val amount: Double,
    val desc: String,
    val type: String,
    val categoryId: Int?,
    val date: String,
    val categoryName: String?,
    val icon: Int = 0
)