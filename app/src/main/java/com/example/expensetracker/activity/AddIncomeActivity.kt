package com.example.expensetracker.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.R
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.databinding.ActivityAddIncomeBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddIncomeActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddIncomeBinding
    private var type: String = "INCOME"
    private lateinit var db: AppDatabase
    private var selectedCategoryId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getInstance(this)
        observeCategories()

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
        binding.btnSaveIncome.setOnClickListener(this)
        binding.tvAddCategory.setOnClickListener(this)
        binding.tvDeleteCategory.setOnClickListener(this)
    }
    private fun saveTransaction() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: return
        val desc = binding.etDescription.text.toString()

//        val transactionType =
//            if (type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE

        // save to DB using transactionType
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)

        val etCategory = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAdd)

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnAdd.setOnClickListener {
            val categoryName = etCategory.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    db.categoryDao().insertCategory(
                        CategoryEntity(
                            name = categoryName,
                            color = Color.RED,
                            iconRes = R.drawable.ic_food,
                        )
                    )
                }
                dialog.dismiss()
            } else {
                etCategory.error = "Required"
            }
        }

        dialog.show()
    }
    private fun showDeleteCategoryDialog() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getAllCategories().first()

            if (categories.isEmpty()) return@launch

            val dialogView = layoutInflater.inflate(R.layout.dialog_delete_category, null)
            val spinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCategories)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

            val adapter = android.widget.ArrayAdapter(
                this@AddIncomeActivity,
                android.R.layout.simple_spinner_item,
                categories.map { it.name }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            val dialog = AlertDialog.Builder(this@AddIncomeActivity)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnDelete.setOnClickListener {
                val selectedIndex = spinner.selectedItemPosition
                val selectedCategory = categories[selectedIndex]

                lifecycleScope.launch(Dispatchers.IO) {
                    db.categoryDao().deleteCategory(selectedCategory)
                }
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSaveIncome -> saveTransaction()
            R.id.tvAddCategory -> showAddCategoryDialog()
            R.id.tvDeleteCategory -> showDeleteCategoryDialog()
        }
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            db.categoryDao().getAllCategories().collect { categories ->
                populateCategoryChips(categories)
            }
        }
    }
    private fun populateCategoryChips(categories: List<CategoryEntity>) {
        binding.chipGroupCategory.removeAllViews()

        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category.name
                isCheckable = true
                tag = category.id

                chipIcon = getDrawable(category.iconRes)
                isChipIconVisible = true

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // Selected category id
                        selectedCategoryId = category.id
                    }
                }
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

}