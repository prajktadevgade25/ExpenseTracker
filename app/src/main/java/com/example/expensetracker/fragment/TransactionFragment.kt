package com.example.expensetracker.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.R
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.databinding.FragmentTransactionBinding
import com.example.expensetracker.ui.transaction.TransactionAdapter
import com.example.expensetracker.ui.transaction.showTransactionBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * TransactionFragment
 *
 * Displays all income & expense transactions.
 * Allows adding a new transaction using BottomSheet.
 */
class TransactionFragment : Fragment(R.layout.fragment_transaction),
    TransactionAdapter.TransactionListener {

    private lateinit var binding: FragmentTransactionBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTransactionBinding.bind(view)
        db = AppDatabase.getInstance(requireContext())

        adapter = TransactionAdapter(mutableListOf(),this)
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        observeTransactions()

        binding.fabAddTransaction.setOnClickListener {
            lifecycleScope.launch {
                val categories = withContext(Dispatchers.IO) {
                    db.categoryDao().getAllCategories().first()
                }

                if (categories.isEmpty()) return@launch

                showTransactionBottomSheet(
                    requireContext(),
                    categories
                ) { title, amount, type, categoryId ->

                    lifecycleScope.launch(Dispatchers.IO) {
                        db.transactionDao().insertTransaction(
                            TransactionEntity(
                                title = title,
                                amount = amount,
                                type = type,
                                categoryId = categoryId,
                                date = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Observes transactions and categories
     * and maps them together for UI display.
     */
    private fun observeTransactions() {
        lifecycleScope.launch {
            combine(
                db.transactionDao().getAllTransactions(),
                db.categoryDao().getAllCategories()
            ) { transactions, categories ->
                transactions.mapNotNull { transaction ->
                    val category = categories.find { it.id == transaction.categoryId }
                    category?.let { Pair(transaction, it) }
                }
            }.collect { result ->
                adapter.updateData(result)
            }
        }
    }

    override fun onEdit(transaction: TransactionEntity) {
        lifecycleScope.launch {
            val categories = db.categoryDao().getAllCategories().first()

            showTransactionBottomSheet(
                requireContext(),
                categories,
                transaction
            ) { title, amount, type, categoryId ->

                lifecycleScope.launch(Dispatchers.IO) {
                    db.transactionDao().updateTransaction(
                        transaction.copy(
                            title = title,
                            amount = amount,
                            type = type,
                            categoryId = categoryId
                        )
                    )
                }
            }
        }
    }

    override fun onDelete(transaction: TransactionEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.transactionDao().deleteTransaction(transaction)
        }
    }
}