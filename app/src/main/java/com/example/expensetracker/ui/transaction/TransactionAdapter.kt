package com.example.expensetracker.ui.transaction

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.data.entity.TransactionEntity
import com.example.expensetracker.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val list: MutableList<Pair<TransactionEntity, CategoryEntity>>,
    private val listener: TransactionListener
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    interface TransactionListener {
        fun onEdit(transaction: TransactionEntity)
        fun onDelete(transaction: TransactionEntity)
    }

    inner class ViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (transaction, _) = list[position]

        holder.binding.tvTitle.text = transaction.title
//        holder.binding.imgIcon.setImageResource(category.iconRes)
//        holder.binding.imgIcon.setColorFilter(category.color)

        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
//        holder.binding.tvDate.text = sdf.format(Date(transaction.date))

        holder.binding.tvAmount.text = if (transaction.type == "EXPENSE") "- ₹${transaction.amount}"
        else "+ ₹${transaction.amount}"

        holder.binding.tvAmount.setTextColor(
            if (transaction.type == "EXPENSE") Color.RED else Color.GREEN
        )

        holder.binding.btnMore.setOnClickListener {
            val popup = PopupMenu(it.context, it)
            popup.inflate(R.menu.menu_transaction)
            popup.setOnMenuItemClickListener { menu ->
                when (menu.itemId) {
                    R.id.action_edit -> {
                        listener.onEdit(transaction)
                        true
                    }

                    R.id.action_delete -> {
                        listener.onDelete(transaction)
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Pair<TransactionEntity, CategoryEntity>>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}