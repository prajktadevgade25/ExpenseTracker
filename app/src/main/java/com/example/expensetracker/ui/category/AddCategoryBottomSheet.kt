package com.example.expensetracker.ui.category

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.databinding.BottomsheetCategoryBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

fun showCategoryBottomSheet(
    context: Context,
    category: CategoryEntity?,
    onSave: (String, Int, Int) -> Unit
) {
    val dialog = BottomSheetDialog(context)
    val binding = BottomsheetCategoryBinding.inflate(LayoutInflater.from(context))
    dialog.setContentView(binding.root)

    var selectedColor = category?.color ?: Color.BLUE
    var selectedIcon = category?.iconRes ?: R.drawable.ic_category

    binding.etCategoryName.setText(category?.name ?: "")

    /* Color Picker */
    val colors = listOf(
        Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
        Color.CYAN, Color.MAGENTA, Color.GRAY, Color.BLACK
    )

    colors.forEach { color ->
        val view = ImageView(context).apply {
            setBackgroundColor(color)
            layoutParams = GridLayout.LayoutParams().apply {
                width = 90
                height = 90
                setMargins(12, 12, 12, 12)
            }
            setOnClickListener { selectedColor = color }
        }
        binding.colorGrid.addView(view)
    }

    /* Icon Picker */
    val icons = listOf(
        R.drawable.ic_food,
        R.drawable.ic_travel,
        R.drawable.ic_shopping,
        R.drawable.ic_bill
    )

    icons.forEach { icon ->
        val img = ImageView(context).apply {
            setImageResource(icon)
            setPadding(16, 16, 16, 16)
            setOnClickListener { selectedIcon = icon }
        }
        binding.iconContainer.addView(img)
    }

    /*Validation + Save */
    binding.btnSave.setOnClickListener {
        val name = binding.etCategoryName.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(context, "Category name required", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

        onSave(name, selectedColor, selectedIcon)
        dialog.dismiss()
    }

    dialog.show()
}