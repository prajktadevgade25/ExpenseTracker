package com.example.expensetracker.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.R
import com.example.expensetracker.activity.ExpensesByCategoryActivity
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.data.model.CategoryTotal
import com.example.expensetracker.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    private var selectedEntry: PieEntry? = null
    private var fromDateMillis: Long = 0L
    private var toDateMillis: Long = 0L


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAnalyticsBinding.bind(view)
        db = AppDatabase.getInstance(requireContext())
        binding.pieChart.isHighlightPerTapEnabled = true

        observeExpenseByCategory()
        setDefaultDates()
        binding.pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectedEntry = e as? PieEntry
            }

            override fun onNothingSelected() {
                selectedEntry = null
            }
        })
        binding.pieChart.onChartGestureListener = object : OnChartGestureListener {

            override fun onChartLongPressed(me: MotionEvent?) {
                selectedEntry?.let { entry ->
                    //openCategoryDetails(entry.label)
                    openExpensesByCategory(entry.label)
                }
            }

            override fun onChartGestureStart(
                me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {
            }

            override fun onChartGestureEnd(
                me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {
            }

            override fun onChartSingleTapped(me: MotionEvent?) {}
            override fun onChartDoubleTapped(me: MotionEvent?) {}
            override fun onChartFling(
                me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float
            ) {
            }

            override fun onChartScale(
                me: MotionEvent?, scaleX: Float, scaleY: Float
            ) {
            }

            override fun onChartTranslate(
                me: MotionEvent?, dX: Float, dY: Float
            ) {
            }
        }
        binding.tvFromDate.setOnClickListener {
            openDatePicker { millis, text ->
                fromDateMillis = millis
                binding.txtFromDate.text = text
                applyDateFilter()
            }
        }

        binding.tvToDate.setOnClickListener {
            openDatePicker { millis, text ->
                toDateMillis = millis
                binding.txtToDate.text = text
                applyDateFilter()
            }
        }

    }

    private fun setDefaultDates() {
        val cal = Calendar.getInstance()

        // TO date = current time
        toDateMillis = cal.timeInMillis

        // FROM date = start of today
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        fromDateMillis = cal.timeInMillis

        lifecycleScope.launch {
            db.transactionDao().getSmallestDate().collect { dateStr ->
                dateStr?.let {
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val millis = sdf.parse(it)?.time ?: System.currentTimeMillis()
                    fromDateMillis = millis
                    binding.txtFromDate.text = sdf.format(Date(millis))
                    binding.txtToDate.text = sdf.format(Date(toDateMillis))
                    applyDateFilter()
                }
            }
        }


    }

    private fun openDatePicker(onSelected: (Long, String) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker().build()

        picker.show(parentFragmentManager, "DATE")

        picker.addOnPositiveButtonClickListener { millis ->
            val displayFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val dateText = displayFormat.format(Date(millis))
            onSelected(millis, dateText)
        }
    }

    private fun applyDateFilter() {
        if (fromDateMillis == 0L || toDateMillis == 0L) return

        lifecycleScope.launch {
            val list = db.transactionDao().getAllExpenseTransactions()

            val sdf = SimpleDateFormat(
                "dd-MM-yyyy HH:mm:ss", Locale.getDefault()
            )

            val filtered = list.filter { tx ->
                val time = sdf.parse(tx.date)?.time ?: 0L
                time in fromDateMillis..(toDateMillis + 86400000)
            }

            val grouped = filtered.groupBy { it.categoryName ?: "Other" }.map { (category, items) ->
                CategoryTotal(
                    categoryName = category, total = items.sumOf { it.amount })
            }

            setupPieChart(grouped)
        }
    }


    private fun openExpensesByCategory(category: String) {

        lifecycleScope.launch {

            val list = db.transactionDao().getTransactionsByCategory(category)

            if (list.isEmpty()) {
                Toast.makeText(
                    requireContext(), "No expenses found", Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            val intent = Intent(
                requireContext(), ExpensesByCategoryActivity::class.java
            )
            intent.putExtra("category", category)
            startActivity(intent)
        }
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
        dataSet.valueTextSize = 16f

        val pieData = PieData(dataSet)
        val legend = binding.pieChart.legend

        binding.pieChart.apply {
            this.data = pieData
            description.isEnabled = false
            isDrawHoleEnabled = true
            legend.textSize = 10f
            holeRadius = 50f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            centerText = context.getString(R.string.expense_by_category)
            setCenterTextSize(14f)
            animateY(900)
            invalidate()
        }
    }
}