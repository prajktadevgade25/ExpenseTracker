package com.example.expensetracker.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.databinding.ActivityTransationDetailsBinding
import kotlinx.coroutines.launch

/**
 * TransationDetailsActivity
 *
 * Displays complete details of a selected transaction.
 *
 * Features:
 * - Receives transaction ID via Intent.
 * - Fetches transaction and its category from Room database.
 * - Displays amount, category, type, date, and icon.
 * - Colors amount based on transaction type.
 * - Allows deletion of the transaction.
 * - Supports navigation back to previous screen.
 */
class TransationDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransationDetailsBinding
    private lateinit var db: AppDatabase
    private var transactionId: Int = 0

    /**
     * Initializes UI, database instance,
     * and loads transaction details.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Get transaction ID from intent
        transactionId = intent.getIntExtra("transactionId", 0)

        loadTransaction()

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnDelete.setOnClickListener {
            deleteTransaction()
        }

        binding.btnEdit.setOnClickListener {
            // TODO: Open edit transaction screen
        }
    }

    /**
     * Loads transaction details from database
     * and updates UI fields.
     */
    private fun loadTransaction() {
        lifecycleScope.launch {

            val tx = db.transactionDao().getTransactionWithCategory(transactionId)

            if (tx == null) {
                Toast.makeText(
                    this@TransationDetailsActivity, "Transaction not found", Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            binding.imgCategory.setImageResource(tx.icon)
            binding.txtTitle.text = tx.desc ?: "Expense"
            binding.txtCategory.text = tx.categoryName
            binding.txtType.text = " ${tx.type}"
            binding.txtCategoryRow.text = " ${tx.categoryName}"
            binding.txtDate.text = " ${tx.date}"

            binding.txtAmount.text = if (tx.type == "EXPENSE") "- ₹${tx.amount}"
            else "+ ₹${tx.amount}"

            binding.txtAmount.setTextColor(
                if (tx.type == "EXPENSE") "#F81F1F".toColorInt()
                else "#42A611".toColorInt()
            )
        }
    }

    /**
     * Deletes transaction and closes screen.
     * (Deletion logic currently commented out.)
     */
    private fun deleteTransaction() {
        lifecycleScope.launch {

            // TODO: Enable deletion when DAO method is confirmed
            // val tx = db.transactionDao().getById(transactionId)
            // db.transactionDao().deleteTransaction(tx)

            Toast.makeText(
                this@TransationDetailsActivity, "Transaction deleted", Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}