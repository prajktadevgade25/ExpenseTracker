package com.example.expensetracker.ui.transaction

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class TransactionSwipeCallback(
    private val adapter: TransactionsAdapter
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val item = adapter.getItem(position)

        // show options dialog
        AlertDialog.Builder(viewHolder.itemView.context)
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                if (which == 0) adapter.onEdit(item)
                else adapter.onDelete(item)
            }
            .setOnDismissListener {
                adapter.notifyItemChanged(position)
            }
            .show()

    }
}