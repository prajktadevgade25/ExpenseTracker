package com.example.expensetracker.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.R
import com.example.expensetracker.activity.AddIncomeActivity
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.databinding.FragmentHomeBinding
import com.example.expensetracker.ui.transaction.TransactionAdapter
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
class HomeFragment : Fragment(R.layout.fragment_home), View.OnClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionAdapter

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

        binding = FragmentHomeBinding.bind(view)
        db = AppDatabase.getInstance(requireContext())
        // Attach click listener
        binding.lnrAddIncome.setOnClickListener(this)
        binding.lnrAddExpense.setOnClickListener(this)
        adapter =
            TransactionAdapter(mutableListOf(), object : TransactionAdapter.TransactionListener {
                override fun onEdit(transaction: TransactionEntity) {}
                override fun onDelete(transaction: TransactionEntity) {}
            })

        binding.rvRecent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecent.adapter = adapter

        observeSummary()
        observeRecentTransactions()
    }

    /**
     * Observes income and expense totals from the database.
     *
     * Combines:
     * - Total income
     * - Total expense
     *
     * Calculates and updates:
     * - Income
     * - Expense
     * - Balance (income - expense)
     */
    private fun observeSummary() {
        lifecycleScope.launch {
            combine(
                db.transactionDao().getTotalIncome(), db.transactionDao().getTotalExpense()
            ) { income, expense ->
                val inc = income ?: 0.0
                val exp = expense ?: 0.0
                Triple(inc, exp, inc - exp)
            }.collect {
                binding.tvIncome.text = "₹${it.first}"
                binding.tvExpense.text = "₹${it.second}"
                binding.tvBalance.text = "₹${it.third}"
            }
        }
    }

    /**
     * Observes recent transactions and their related categories.
     *
     * Combines:
     * - Recent transactions list
     * - Category list
     *
     * Maps each transaction to its corresponding category
     * and updates the RecyclerView adapter.
     */
    private fun observeRecentTransactions() {
        lifecycleScope.launch {
            combine(
                db.transactionDao().getRecentTransactions(), db.categoryDao().getAllCategories()
            ) { transactions, categories ->
                transactions.mapNotNull { t ->
                    val cat = categories.find { it.id == t.categoryId }
                    cat?.let { Pair(t, it) }
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
            R.id.lnrAddIncome -> {
                val intent = Intent(requireContext(), AddIncomeActivity::class.java)
                intent.putExtra(getString(R.string.type), getString(R.string.income))
                startActivity(intent)
            }

            R.id.lnrAddExpense -> {
                val intent = Intent(requireContext(), AddIncomeActivity::class.java)
                intent.putExtra(getString(R.string.type), getString(R.string.expense))
                startActivity(intent)
            }
        }
    }
}