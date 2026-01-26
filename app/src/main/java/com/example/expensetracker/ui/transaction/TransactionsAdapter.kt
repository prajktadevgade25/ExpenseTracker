package com.example.expensetracker.ui.transaction

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.databinding.ItemRecentTransactionBinding

/**
 * RecyclerView Adapter for displaying a list of transactions along with their categories.
 *
 * Each item in the list contains:
 * - [TransactionEntity] → transaction details (amount, description, type, date)
 * - [CategoryEntity] → category information (name, etc.)
 *
 * @property list Mutable list of transaction-category pairs displayed in RecyclerView
 */
class TransactionsAdapter(
    private val list: MutableList<Pair<TransactionEntity, CategoryEntity>>,
) : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {

    /**
     * ViewHolder class that holds the binding for a single transaction item view.
     *
     * @property binding ViewBinding for `item_recent_transaction.xml`
     */
    inner class ViewHolder(val binding: ItemRecentTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Inflates the transaction item layout and creates a [ViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRecentTransactionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    /**
     * Binds transaction and category data to the UI components.
     *
     * - Shows category name
     * - Displays transaction description and date
     * - Formats amount with + / - based on transaction type
     * - Applies color based on EXPENSE or INCOME
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (transaction, category) = list[position]

        holder.binding.tvCategory.text = category.name
        holder.binding.tvDesc.text = transaction.desc
        holder.binding.date.text = transaction.date

        holder.binding.tvAmount.text = if (transaction.type == "EXPENSE") "- ₹${transaction.amount}"
        else "+ ₹${transaction.amount}"

        holder.binding.tvAmount.setTextColor(
            if (transaction.type == "EXPENSE") Color.RED else Color.GREEN
        )
    }

    /**
     * Returns the total number of items in the adapter.
     */
    override fun getItemCount(): Int = list.size

    /**
     * Updates the adapter data with a new list of transactions.
     *
     * @param newList New list of transaction-category pairs
     */
    fun updateData(newList: List<Pair<TransactionEntity, CategoryEntity>>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}