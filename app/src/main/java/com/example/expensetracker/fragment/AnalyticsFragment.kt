package com.example.expensetracker.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.R
import com.example.expensetracker.data.model.CategoryTotal
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

/**
 * AnalyticsFragment
 *
 * Displays visual analytics for the Expense Tracker application.
 *
 * Current Features:
 * - Shows category-wise expense distribution using a PieChart
 * - Observes Room database using Kotlin Flow
 * - Updates chart reactively when data changes
 *
 * This fragment helps users understand where their
 * money is being spent across different categories.
 */
class AnalyticsFragment : Fragment(R.layout.fragment_analytics) {

    private lateinit var binding: FragmentAnalyticsBinding
    private lateinit var db: AppDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAnalyticsBinding.bind(view)
        db = AppDatabase.getInstance(requireContext())

        observeExpenseByCategory()
    }

    /**
     * Observes category-wise expense data from the database.
     *
     * Uses Kotlin Flow to:
     * - Listen for changes in expense data
     * - Automatically update the PieChart when data changes
     */
    private fun observeExpenseByCategory() {
        lifecycleScope.launch {
            db.transactionDao().getExpenseByCategory().collect { list ->
                setupPieChart(list)
            }
        }
    }

    /**
     * Configures and renders the PieChart based on category-wise expense data.
     *
     * @param data List of category expense totals used to populate the chart
     *
     * Responsibilities:
     * - Converts data into PieEntry objects
     * - Applies colors and styling
     * - Animates and refreshes the chart
     */
    private fun setupPieChart(data: List<CategoryTotal>) {

        val entries = data.map {
            PieEntry(it.total.toFloat(), it.categoryName)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            "#EF5350".toColorInt(),
            "#AB47BC".toColorInt(),
            "#5C6BC0".toColorInt(),
            "#29B6F6".toColorInt(),
            "#66BB6A".toColorInt(),
            "#FFCA28".toColorInt(),
            "#FFA726".toColorInt()
        )
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val pieData = PieData(dataSet)

        binding.pieChart.apply {
            this.data = pieData
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 50f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(10f)
            centerText = context.getString(R.string.expense_by_category)
            setCenterTextSize(12f)
            animateY(900)
            invalidate()
        }
    }
}