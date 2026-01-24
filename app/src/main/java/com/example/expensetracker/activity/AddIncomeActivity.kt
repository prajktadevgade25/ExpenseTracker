package com.example.expensetracker.activity

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.expensetracker.R
import com.example.expensetracker.databinding.ActivityAddIncomeBinding

class AddIncomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddIncomeBinding
    private var type: String = "INCOME"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        type = intent.getStringExtra("TYPE") ?: "INCOME"

        setupUI()
        setupClicks()

    }
    private fun setupUI() {
        if (type == "INCOME") {
            binding.tvTitle.text = "Add Income"
            binding.btnSaveIncome.text = "Save Income"
            binding.btnSaveIncome.backgroundTintList =
                ColorStateList.valueOf(getColor(R.color.black))

        } else {
            binding.tvTitle.text = "Add Expense"
            binding.btnSaveIncome.text = "Save Expense"
            binding.btnSaveIncome.backgroundTintList =
                ColorStateList.valueOf(getColor(R.color.black))
        }
    }
    private fun setupClicks() {
        binding.btnSaveIncome.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: return
        val desc = binding.etDescription.text.toString()

//        val transactionType =
//            if (type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE

        // save to DB using transactionType
    }
}