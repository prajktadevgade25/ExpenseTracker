package com.example.expensetracker.activity

import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.R
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.databinding.ActivityAddIncomeBinding
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddIncomeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddIncomeBinding
    private lateinit var db: AppDatabase

    private var selectedCategoryId: Int? = null
    private var transactionId: Int = 0
    private var existingTransaction: TransactionEntity? = null
    private lateinit var type: String
    private lateinit var defaultCategories: Set<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        transactionId = intent.getIntExtra("transactionId", 0)
        type = intent.getStringExtra(getString(R.string.type))
            ?: getString(R.string.income)

        defaultCategories = setOf(
            getString(R.string.salary),
            getString(R.string.gift),
            getString(R.string.refund),
            getString(R.string.investment),
            getString(R.string.other)
        )

        setupUI()
        setupClicks()
        observeCategories()

        if (transactionId != 0) {
            loadTransactionForEdit()
        }
    }

    private fun setupUI() {
        val sdf = SimpleDateFormat(
            "dd-MM-yyyy HH:mm:ss",
            Locale.getDefault()
        )
        binding.tvDateTime.text = sdf.format(Date())

        if (type == getString(R.string.income)) {
            binding.tvTitle.text = getString(R.string.add_income)
            binding.btnSaveIncome.text = getString(R.string.save_income)
        } else {
            binding.tvTitle.text = getString(R.string.add_expense)
            binding.btnSaveIncome.text = getString(R.string.save_expense)
        }
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            db.categoryDao().getAllCategories().collect {
                populateCategoryChips(it)
            }
        }
    }

    private fun populateCategoryChips(categories: List<CategoryEntity>) {
        binding.chipGroupCategory.removeAllViews()

        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category.name
                setTextColor(Color.BLACK)
                isCheckable = true
                tag = category.id

                chipIcon = getDrawable(category.iconRes)
                chipBackgroundColor =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(context, category.color)
                    )

                isChipIconVisible = true

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedCategoryId = category.id
                    }
                }

                // Pre-select category when editing
                if (category.id == selectedCategoryId) {
                    isChecked = true
                }
            }

            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun setupClicks() {
        binding.btnSaveIncome.setOnClickListener(this)
        binding.tvAddCategory.setOnClickListener(this)
        binding.tvDeleteCategory.setOnClickListener(this)
        binding.lnrDate.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSaveIncome -> saveTransaction()
            R.id.tvAddCategory -> showAddCategoryDialog()
            R.id.tvDeleteCategory -> showDeleteCategoryDialog()
            R.id.lnrDate -> showDatePicker()
            R.id.imgBack -> onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun saveTransaction() {

        val amountText = binding.etAmount.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()

        if (amountText.isEmpty()) {
            binding.etAmount.error = getString(R.string.enter_amount)
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = getString(R.string.invalid_amount)
            return
        }

        if (desc.isEmpty()) {
            binding.etDescription.error = getString(R.string.enter_description)
            return
        }

        val date = binding.tvDateTime.text.toString()

        lifecycleScope.launch(Dispatchers.IO) {

            if (existingTransaction != null) {
                val updated = existingTransaction!!.copy(
                    amount = amount,
                    desc = desc,
                    type = type,
                    categoryId = selectedCategoryId,
                    date = date
                )
                db.transactionDao().updateTransaction(updated)

                finish()
            } else {
                db.transactionDao().insertTransaction(
                    TransactionEntity(
                        amount = amount,
                        desc = desc,
                        type = type,
                        categoryId = selectedCategoryId,
                        date = date
                    )
                )
            }
        }

        finish()
    }

    private fun showAddCategoryDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_category, null)

        val etCategory = view.findViewById<EditText>(R.id.etCategoryName)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnAdd).setOnClickListener {
            val name = etCategory.text.toString().trim()

            if (name.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    db.categoryDao().insertCategory(
                        CategoryEntity(
                            name = name,
                            color = R.color.light_purple,
                            iconRes = R.drawable.ic_other
                        )
                    )
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteCategoryDialog() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getAllCategories().first()

            val deletable = categories.filter {
                it.name !in defaultCategories
            }

            if (deletable.isEmpty()) return@launch

            val view = layoutInflater.inflate(R.layout.dialog_delete_category, null)
            val spinner =
                view.findViewById<android.widget.Spinner>(R.id.spinnerCategories)

            spinner.adapter = android.widget.ArrayAdapter(
                this@AddIncomeActivity,
                android.R.layout.simple_spinner_item,
                deletable.map { it.name }
            )

            val dialog = AlertDialog.Builder(this@AddIncomeActivity)
                .setView(view).create()

            view.findViewById<Button>(R.id.btnDelete).setOnClickListener {
                val cat = deletable[spinner.selectedItemPosition]
                lifecycleScope.launch(Dispatchers.IO) {
                    db.categoryDao().deleteCategory(cat)
                }
                dialog.dismiss()
            }

            view.findViewById<Button>(R.id.btnCancel)
                .setOnClickListener { dialog.dismiss() }

            dialog.show()
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.show(supportFragmentManager, "date")

        picker.addOnPositiveButtonClickListener {
            showTimePicker(it)
        }
    }

    private fun showTimePicker(dateMillis: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis

        TimePickerDialog(
            this,
            { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)

                // DB format
                val dbFormat = SimpleDateFormat(
                    "dd-MM-yyyy HH:mm:ss",
                    Locale.getDefault()
                )

                val formattedDateTime = dbFormat.format(cal.time)

                // Save or display
                binding.tvDateTime.text = formattedDateTime
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun loadTransactionForEdit() {
        lifecycleScope.launch {
            existingTransaction =
                db.transactionDao().getById(transactionId)

            existingTransaction?.let { tx ->
                binding.etAmount.setText(tx.amount.toString())
                binding.etDescription.setText(tx.desc)
                binding.tvDateTime.text = tx.date
                type = tx.type

                selectedCategoryId = tx.categoryId
            }
        }
    }
}