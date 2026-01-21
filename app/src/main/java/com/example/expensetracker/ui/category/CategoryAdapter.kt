package com.example.expensetracker.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.databinding.ItemCategoryBinding

/**
 * CategoryAdapter
 *
 * RecyclerView adapter responsible for displaying
 * the list of expense categories.
 *
 * @property list Mutable list of categories displayed in RecyclerView
 * @property listener Listener to handle edit and delete actions
 */
class CategoryAdapter(
    private val list: MutableList<CategoryEntity>, private val listener: CategoryListener
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    /**
     * Listener interface to handle category item interactions.
     */
    interface CategoryListener {
        /**
         * Called when a category item is clicked.
         *
         * @param category CategoryEntity to be edited
         */
        fun onEdit(category: CategoryEntity)

        /**
         * Called when a category item is long-pressed.
         *
         * @param category CategoryEntity to be deleted
         */
        fun onDelete(category: CategoryEntity)
    }

    /**
     * ViewHolder class that holds the view binding
     * for each category item.
     */
    inner class ViewHolder(
        val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root)

    /**
     * Inflates the item_category layout and
     * creates a new ViewHolder instance.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    /**
     * Binds category data to the ViewHolder UI.
     */
    override fun onBindViewHolder(
        holder: ViewHolder, position: Int
    ) {
        val category = list[position]

        // Set category name
        holder.binding.tvCategoryName.text = category.name

        // Set icon
        holder.binding.imgIcon.setImageResource(category.iconRes)

        // Set category color
        holder.binding.imgIcon.setColorFilter(category.color)

        // Click → Edit category
        holder.binding.root.setOnClickListener {
            listener.onEdit(category)
        }

        // Long click → Delete category
        holder.binding.root.setOnLongClickListener {
            listener.onDelete(category)
            true
        }
    }

    /**
     * Returns the number of categories in the list.
     */
    override fun getItemCount(): Int = list.size

    /**
     * Updates the RecyclerView with a new category list.
     *
     * @param newList Latest list of categories from database
     */
    fun updateData(newList: List<CategoryEntity>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}