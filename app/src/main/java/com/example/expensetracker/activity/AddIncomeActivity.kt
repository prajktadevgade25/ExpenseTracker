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

/**
 * AddIncomeActivity
 *
 * This screen is used to add a new Income or Expense transaction.
 *
 * Features:
 * - Select income or expense type
 * - Choose category using ChipGroup
 * - Add or delete custom categories
 * - Select date and time using Material DatePicker & TimePicker
 * - Save transaction details (amount, description, category, date-time)
 */
class AddIncomeActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddIncomeBinding
    lateinit var type: String
    lateinit var defaultCategories: Set<String>
    private lateinit var db: AppDatabase
    private var selectedCategoryId: Int? = null

    /**
     * Initializes the activity.
     *
     * - Sets up ViewBinding
     * - Reads transaction type (INCOME / EXPENSE) from intent
     * - Initializes database instance
     * - Observes categories from database
     * - Sets up UI and click listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        type = getString(R.string.income)

        /**
         * List of default categories that cannot be deleted by user.
         */
        defaultCategories = setOf(
            getString(R.string.salary),
            getString(R.string.gift),
            getString(R.string.refund),
            getString(R.string.investment),
            getString(R.string.other)
        )
        db = AppDatabase.getInstance(this)
        observeCategories()

        type = intent.getStringExtra(getString(R.string.type)) ?: getString(R.string.income)

        setupUI()
        setupClicks()

    }

    /**
     * Configures UI elements based on transaction type.
     *
     * - Updates title text
     * - Updates save button text
     * - Changes button color if needed
     */
    private fun setupUI() {
        val sdf = SimpleDateFormat(
            getString(R.string.eee_mmm_d_h_mm_a), Locale.getDefault()
        )

        binding.tvDateTime.text = sdf.format(Date())
        if (type == getString(R.string.income)) {
            binding.tvTitle.text = getString(R.string.add_income)
            binding.btnSaveIncome.text = getString(R.string.save_income)
            binding.btnSaveIncome.backgroundTintList =
                ColorStateList.valueOf(getColor(R.color.black))

        } else {
            binding.tvTitle.text = getString(R.string.add_expense)
            binding.btnSaveIncome.text = getString(R.string.save_expense)
            binding.btnSaveIncome.backgroundTintList =
                ColorStateList.valueOf(getColor(R.color.black))
        }
    }

    /**
     * Observes category list from database using Flow.
     *
     * Automatically updates UI whenever categories change.
     */
    private fun observeCategories() {
        lifecycleScope.launch {
            db.categoryDao().getAllCategories().collect { categories ->
                populateCategoryChips(categories)
            }
        }
    }

    /**
     * Dynamically creates category chips and adds them to ChipGroup.
     *
     * @param categories List of CategoryEntity from database
     *
     * - Displays category name and icon
     * - Allows single category selection
     * - Stores selected category ID
     */
    private fun populateCategoryChips(categories: List<CategoryEntity>) {
        binding.chipGroupCategory.removeAllViews()

        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category.name
                isCheckable = true
                tag = category.id

                chipIcon = getDrawable(category.iconRes)
                chipBackgroundColor = ColorStateList.valueOf(category.color)

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

    /**
     * Attaches click listeners to all interactive UI elements.
     */
    private fun setupClicks() {
        binding.btnSaveIncome.setOnClickListener(this)
        binding.tvAddCategory.setOnClickListener(this)
        binding.tvDeleteCategory.setOnClickListener(this)
        binding.lnrDate.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
    }

    /**
     * Handles click events for all registered views.
     *
     * @param v The clicked view
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSaveIncome -> saveTransaction()
            R.id.tvAddCategory -> showAddCategoryDialog()
            R.id.tvDeleteCategory -> showDeleteCategoryDialog()
            R.id.lnrDate -> showDatePicker()
            R.id.imgBack -> onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * Validates user input and prepares transaction data.
     *
     * - Reads amount and description
     * - Determines transaction type (Income / Expense)
     * - Intended to save transaction into database
     */
    private fun saveTransaction() {

        val amountText = binding.etAmount.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()

        // Amount validation
        if (amountText.isEmpty()) {
            binding.etAmount.error = getString(R.string.enter_amount)
            binding.etAmount.requestFocus()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = getString(R.string.invalid_amount)
            binding.etAmount.requestFocus()
            return
        }

        // Description validation
        if (desc.isEmpty()) {
            binding.etDescription.error = getString(R.string.enter_description)
            binding.etDescription.requestFocus()
            return
        }

        val date = binding.tvDateTime.text.toString()

        lifecycleScope.launch(Dispatchers.IO) {
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

        onBackPressedDispatcher.onBackPressed()
    }

    /**
     * Displays dialog to add a new custom category.
     *
     * - Takes category name input
     * - Inserts category into database
     * - Uses default color and icon
     */
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
                            color = Color.LTGRAY,
                            iconRes = R.drawable.ic_other,
                        )
                    )
                }
                dialog.dismiss()
            } else {
                etCategory.error = getString(R.string.required)
            }
        }

        dialog.show()
    }

    /**
     * Displays dialog to delete user-created categories.
     *
     * - Filters out default categories
     * - Shows remaining categories in spinner
     * - Deletes selected category from database
     */
    private fun showDeleteCategoryDialog() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getAllCategories().first()

            // remove default categories
            val deletableCategories = categories.filter {
                it.name !in defaultCategories
            }

            if (deletableCategories.isEmpty()) {
                AlertDialog.Builder(this@AddIncomeActivity)
                    .setMessage(getString(R.string.no_custom_categories_to_delete))
                    .setPositiveButton("OK", null).show()
                return@launch
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_delete_category, null)
            val spinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCategories)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

            val adapter = android.widget.ArrayAdapter(
                this@AddIncomeActivity,
                android.R.layout.simple_spinner_item,
                deletableCategories.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            val dialog = AlertDialog.Builder(this@AddIncomeActivity).setView(dialogView).create()

            btnCancel.setOnClickListener { dialog.dismiss() }

            btnDelete.setOnClickListener {
                val selectedCategory = deletableCategories[spinner.selectedItemPosition]

                lifecycleScope.launch(Dispatchers.IO) {
                    db.categoryDao().deleteCategory(selectedCategory)
                }
                dialog.dismiss()
            }

            dialog.show()
        }
    }


    /**
     * Opens Material Date Picker to select a date.
     *
     * After date selection, automatically opens Time Picker.
     */
    private fun showDatePicker() {

        val picker =
            MaterialDatePicker.Builder.datePicker().setTitleText(getString(R.string.select_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()

        picker.show(supportFragmentManager, getString(R.string.date_picker))

        picker.addOnPositiveButtonClickListener { selectedDateMillis ->
            showTimePicker(selectedDateMillis)
        }
    }

    /**
     * Opens TimePicker dialog after date selection.
     *
     * @param selectedDateMillis Selected date in milliseconds
     *
     * Combines date and time and formats it as:
     * "EEE, MMM d, h:mm a"
     */
    private fun showTimePicker(selectedDateMillis: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDateMillis

        val timePicker = TimePickerDialog(
            this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)

                val sdf =
                    SimpleDateFormat(getString(R.string.eee_mmm_d_h_mm_a), Locale.getDefault())
                binding.tvDateTime.text = sdf.format(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
        )

        timePicker.show()
    }
}