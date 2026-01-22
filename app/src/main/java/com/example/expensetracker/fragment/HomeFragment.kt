package com.example.expensetracker.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.R
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.databinding.FragmentHomeBinding
import com.example.expensetracker.ui.transaction.TransactionAdapter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: TransactionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        db = AppDatabase.getInstance(requireContext())

        adapter = TransactionAdapter(mutableListOf(), object :
            TransactionAdapter.TransactionListener {
            override fun onEdit(transaction: TransactionEntity) {}
            override fun onDelete(transaction: TransactionEntity) {}
        })

        binding.rvRecent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecent.adapter = adapter

        observeSummary()
        observeRecentTransactions()
    }

    private fun observeSummary() {
        lifecycleScope.launch {
            combine(
                db.transactionDao().getTotalIncome(),
                db.transactionDao().getTotalExpense()
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

    private fun observeRecentTransactions() {
        lifecycleScope.launch {
            combine(
                db.transactionDao().getRecentTransactions(),
                db.categoryDao().getAllCategories()
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
}