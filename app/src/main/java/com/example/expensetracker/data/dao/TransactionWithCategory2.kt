package com.example.expensetracker.data.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.data.entity.TransactionEntity

data class TransactionWithCategory2(
    @Embedded val transaction: TransactionEntity, @Relation(
        parentColumn = "categoryId", entityColumn = "id"
    ) val category: CategoryEntity?
)

