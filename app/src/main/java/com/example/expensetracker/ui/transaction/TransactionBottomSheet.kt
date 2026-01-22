package com.example.expensetracker.ui.transaction

import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.databinding.BottomsheetTransactionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * BottomSheet for Add / Edit Transaction
 *
 * @param context Context
 * @param categories List of categories
 * @param transaction Transaction to edit (null for add)
 * @param onSave Callback when user saves transaction
 */
fun showTransactionBottomSheet(
    context: Context,
    categories: List<CategoryEntity>,
    transaction: TransactionEntity? = null,
    onSave: (String, Double, String, Int) -> Unit
) {
    val dialog = BottomSheetDialog(context)
    val binding = BottomsheetTransactionBinding.inflate(LayoutInflater.from(context))
    dialog.setContentView(binding.root)

    /* ---------------- CATEGORY DROPDOWN ---------------- */

    val categoryNames = categories.map { it.name }

    val categoryAdapter = ArrayAdapter(
        context,
        android.R.layout.simple_list_item_1,
        categoryNames
    )

    binding.etCategory.setAdapter(categoryAdapter)

    /* ---------------- PREFILL DATA (EDIT MODE) ---------------- */

    transaction?.let {
        binding.tvTitle.setText(it.title)
        binding.etAmount.setText(it.amount.toString())

        if (it.type == "INCOME") {
            binding.rbIncome.isChecked = true
        } else {
            binding.rbExpense.isChecked = true
        }

        val cat = categories.firstOrNull { c -> c.id == it.categoryId }
        binding.etCategory.setText(cat?.name ?: "", false)
    }

    /* ---------------- SAVE BUTTON ---------------- */

    binding.btnSave.setOnClickListener {

        val title = binding.tvTitle.text.toString().trim()
        val amountText = binding.etAmount.text.toString().trim()
        val amount = amountText.toDoubleOrNull()

        val type = if (binding.rbExpense.isChecked) {
            "EXPENSE"
        } else {
            "INCOME"
        }

        val selectedCategoryName = binding.etCategory.text.toString()

        val category = categories.firstOrNull {
            it.name == selectedCategoryName
        }

        /* ---------------- VALIDATION ---------------- */

        when {
            title.isEmpty() -> {
                Toast.makeText(context, "Enter title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            amount == null || amount <= 0 -> {
                Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            category == null -> {
                Toast.makeText(context, "Select category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        /* ---------------- CALLBACK ---------------- */

        onSave(
            title,
            amount,
            type,
            category.id
        )

        dialog.dismiss()
    }

    dialog.show()
}