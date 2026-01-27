package com.example.expensetracker.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.R
import com.example.expensetracker.activity.AddIncomeActivity
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.databinding.FragmentTransactionBinding
import com.example.expensetracker.ui.transaction.TransactionsAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * HomeFragment
 *
 * Displays the main dashboard of the Expense Tracker app.
 *
 * Features:
 * - Shows total income, total expense, and current balance
 * - Displays recent transactions in a RecyclerView
 * - Allows quick navigation to add Income or Expense
 *
 * Data is observed reactively using Room + Kotlin Flow.
 */
class TransactionFragment : Fragment(R.layout.fragment_transaction), View.OnClickListener {

    private lateinit var binding: FragmentTransactionBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionsAdapter

    /**
     * Called after the fragment's view has been created.
     *
     * - Initializes ViewBinding
     * - Sets up database instance
     * - Configures RecyclerView and adapter
     * - Registers click listeners
     * - Starts observing summary and recent transactions
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTransactionBinding.bind(view)
        db = AppDatabase.getInstance(requireContext())

        adapter = TransactionsAdapter(mutableListOf())
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        // Chip click listeners
        binding.chipAll.setOnClickListener(this)
        binding.chipIncome.setOnClickListener(this)
        binding.chipExpense.setOnClickListener(this)

        getAllTransaction()
    }

    /**
     * Observes recent transactions and their related categories.
     *
     * Combines:
     * - transactions list
     * - Category list
     *
     * Maps each transaction to its corresponding category
     * and updates the RecyclerView adapter.
     *
     * Common observer for All / Income / Expense
     */
    private fun observeTransactions(
        transactionFlow: Flow<List<TransactionEntity>>
    ) {
        lifecycleScope.launch {
            combine(
                transactionFlow, db.categoryDao().getAllCategories()
            ) { transactions, categories ->
                transactions.mapNotNull { transaction ->
                    val category = categories.find { it.id == transaction.categoryId }
                    category?.let { Pair(transaction, it) }
                }
            }.collect {
                adapter.updateData(it)
            }
        }
    }

    /**
     * Handles click events for Add Income and Add Expense buttons.
     *
     * Navigates to [AddIncomeActivity] with appropriate transaction type.
     *
     * @param v The clicked view
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.chipAll -> getAllTransaction()
            R.id.chipIncome -> getAllIncome()
            R.id.chipExpense -> getAllExpense()
        }
    }

    private fun getAllTransaction() {
        observeTransactions(db.transactionDao().getAllTransactions())
    }

    private fun getAllIncome() {
        observeTransactions(db.transactionDao().getIncomeTransactions())
    }

    private fun getAllExpense() {
        observeTransactions(db.transactionDao().getExpenseTransactions())
    }
}