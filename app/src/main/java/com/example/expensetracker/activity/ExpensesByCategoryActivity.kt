package com.example.expensetracker.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.databinding.ActivityExpensesByCategoryBinding
import com.example.expensetracker.ui.transaction.TransactionSwipeCallback
import com.example.expensetracker.ui.transaction.TransactionsAdapter
import kotlinx.coroutines.launch

/**
 * ExpensesByCategoryActivity
 *
 * Displays all expense transactions belonging to a selected category.
 *
 * Features:
 * - Receives category name via Intent.
 * - Fetches transactions from Room database.
 * - Displays transactions in RecyclerView.
 * - Opens transaction details on item click.
 */
class ExpensesByCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpensesByCategoryBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionsAdapter
    private var categoryName: String? = null

    /**
     * Initializes UI, database, and RecyclerView.
     * Also retrieves category name from Intent.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExpensesByCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Category passed from previous screen
        categoryName = intent.getStringExtra("category")

        // Adapter click opens transaction details
        adapter = TransactionsAdapter(
            mutableListOf(),

            // Item click → open details
            onItemClick = { transaction ->
                val intent = Intent(this, TransationDetailsActivity::class.java)
                intent.putExtra("transactionId", transaction.id)
                startActivity(intent)
            },

            // Edit click
            onEditClick = { transaction ->
                val intent = Intent(this, AddIncomeActivity::class.java)
                intent.putExtra("transactionId", transaction.id)
                intent.putExtra("type", transaction.type)
                startActivity(intent)
            },

            // Delete click
            onDeleteClick = { transaction ->
                lifecycleScope.launch {
                    db.transactionDao().deleteTransaction(transaction)
                    // loadTransactions() // reload list
                }
            })

        val swipeCallback = TransactionSwipeCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvTransactions)

        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter

        loadTransactions()
    }

    /**
     * Loads transactions for selected category from database
     * and updates RecyclerView.
     */
    private fun loadTransactions() {
        lifecycleScope.launch {

            val list = db.transactionDao().getTransactionsByCategory2(categoryName ?: "")

            // Close screen if no data found
            if (list.isEmpty()) {
                finish()
                return@launch
            }

            val pairs = list.map {
                Pair(it.transaction, it.category!!)
            }

            adapter.updateData(pairs)
        }
    }
}