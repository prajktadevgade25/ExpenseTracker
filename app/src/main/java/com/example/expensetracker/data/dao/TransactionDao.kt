package com.example.expensetracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.data.model.CategoryTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE type='INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type='EXPENSE'")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 5")
    fun getRecentTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'INCOME' ORDER BY date DESC")
    fun getIncomeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'EXPENSE' ORDER BY date DESC")
    fun getExpenseTransactions(): Flow<List<TransactionEntity>>

    @Query(" SELECT c.name AS categoryName, SUM(t.amount) AS total FROM transactions t INNER JOIN categories c ON t.categoryId = c.id WHERE t.type = 'EXPENSE' GROUP BY t.categoryId")
    fun getExpenseByCategory(): Flow<List<CategoryTotal>>

    @Query("SELECT COUNT(*) > 0 FROM transactions WHERE date = :today AND type = 'INCOME'")
    fun hasSavingsToday(today: String): Boolean

    @Query(" SELECT t.* FROM transactions t INNER JOIN categories c ON t.categoryId = c.id WHERE c.name = :category AND type = 'EXPENSE' ORDER BY t.date DESC")
    suspend fun getTransactionsByCategory(
        category: String
    ): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Int): TransactionEntity?

    @Query(" SELECT t.id, t.amount, t.`desc`, t.type, t.categoryId, t.date, c.name AS categoryName, c.iconRes As icon FROM transactions t LEFT JOIN categories c ON t.categoryId = c.id WHERE t.id = :id")
    suspend fun getTransactionWithCategory(id: Int): TransactionWithCategory?

    @Transaction
    @Query("SELECT * FROM transactions WHERE categoryId IN ( SELECT id FROM categories WHERE name = :category) AND type = 'EXPENSE' ORDER BY date DESC")
    suspend fun getTransactionsByCategory2(
        category: String
    ): List<TransactionWithCategory2>

    @Query("SELECT c.name as categoryName, SUM(t.amount) as total FROM transactions t LEFT JOIN categories c ON t.categoryId = c.id WHERE t.type = 'EXPENSE' AND date BETWEEN :from AND :to GROUP BY c.name")
    suspend fun getExpenseByCategoryBetween(
        from: String, to: String
    ): List<CategoryTotal>

    @Query("SELECT t.id, t.amount, t.`desc`, t.type, t.categoryId, t.date, c.name AS categoryName, c.iconRes AS icon FROM transactions t LEFT JOIN categories c ON t.categoryId = c.id WHERE t.type = 'EXPENSE'")
    suspend fun getAllExpenseTransactions(): List<TransactionWithCategory>

    @Query("SELECT date FROM transactions ORDER BY date ASC LIMIT 1")
    fun getSmallestDate(): Flow<String?>

}
