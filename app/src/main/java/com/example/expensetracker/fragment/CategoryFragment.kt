package com.example.expensetracker.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.R
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.databinding.FragmentCategoryBinding
import com.example.expensetracker.ui.category.CategoryAdapter
import com.example.expensetracker.ui.category.showCategoryBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * CategoryFragment
 *
 * Displays and manages expense categories.
 * Supports:
 * - Viewing categories
 * - Adding categories
 * - Editing categories
 * - Deleting categories
 *
 * Uses Room database for persistence.
 */
class CategoryFragment :
    Fragment(R.layout.fragment_category),
    CategoryAdapter.CategoryListener {

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: CategoryAdapter

    /**
     * Called when fragment view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCategoryBinding.bind(view)
        db = AppDatabase.getInstance(requireContext())

        adapter = CategoryAdapter(mutableListOf(), this)
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter

        loadCategories()

        // ➕ Add Category
        binding.fabAddCategory.setOnClickListener {
            showCategoryBottomSheet(requireContext(), null) { name, color, icon ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    db.categoryDao().insertCategory(
                        CategoryEntity(
                            name = name,
                            color = color,
                            iconRes = icon
                        )
                    )
                    withContext(Dispatchers.Main) {
                        loadCategories()
                    }
                }
            }
        }
    }

    /**
     * Loads categories from Room database
     */
    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            db.categoryDao()
                .getAllCategories()
                .collect { categories ->
                    adapter.updateData(categories)
                }
        }
    }

    /**
     * Edit category callback
     */
    override fun onEdit(category: CategoryEntity) {
        showCategoryBottomSheet(requireContext(), category) { name, color, icon ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                db.categoryDao().updateCategory(
                    category.copy(
                        name = name,
                        color = color,
                        iconRes = icon
                    )
                )
                withContext(Dispatchers.Main) {
                    loadCategories()
                }
            }
        }
    }

    /**
     * Delete category callback
     */
    override fun onDelete(category: CategoryEntity) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            db.categoryDao().deleteCategory(category)
            withContext(Dispatchers.Main) {
                loadCategories()
            }
        }
    }
}