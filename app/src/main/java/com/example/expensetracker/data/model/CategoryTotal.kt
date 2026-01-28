package com.example.expensetracker.data.model

/**
 * Represents total expense grouped by a single category.
 *
 * This model is used for analytics purposes such as
 * displaying category-wise expense distribution
 * in charts (e.g., PieChart).
 *
 * @property categoryName Name of the expense category
 * @property total Total expense amount for the category
 */
data class CategoryTotal(
    val categoryName: String, val total: Double
)