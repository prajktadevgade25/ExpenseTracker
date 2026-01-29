package com.example.expensetracker.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.R
import com.example.expensetracker.activity.LoginActivity
import com.example.expensetracker.data.db.AppDatabase
import com.example.expensetracker.databinding.FragmentSettingsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Settings screen fragment
 * Handles app settings like logout
 */
class SettingsFragment : Fragment(R.layout.fragment_settings), View.OnClickListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase

    /**
     * Called after the fragment's view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSettingsBinding.bind(view)
        db = AppDatabase.getInstance(requireContext())

        binding.btnLogout.setOnClickListener(this)
        binding.btnExport.setOnClickListener(this)
    }

    /**
     * Logs out the current user from Firebase and Google
     * and redirects to LoginActivity
     */
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        GoogleSignIn.getClient(
            requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN
        ).signOut()

        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    /**
     * Handles all click events for this fragment.
     */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnLogout -> logoutUser()
            R.id.btnExport -> showExportDialog()
        }
    }

    /**
     * Displays a dialog that allows the user to export transaction data.
     *
     * The dialog provides options to:
     * - Export transactions as a PDF file
     * - Cancel and dismiss the dialog
     *
     * When the user selects PDF export, the export process is triggered
     * and the dialog is dismissed.
     */
    private fun showExportDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_export, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

        view.findViewById<View>(R.id.btnExportPdf).setOnClickListener {
            exportToPdf()
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Exports all transactions into a PDF file in tabular format.
     *
     * This function:
     * - Fetches all transactions from Room database using Flow
     * - Converts the Flow into a one-time List snapshot
     * - Generates a PDF document with table columns:
     *   Date | Type | Amount | Note
     * - Automatically handles page breaks
     * - Saves the PDF file in the device Downloads directory
     *
     * All heavy operations run on the IO dispatcher.
     * A success message is shown on the main thread after completion.
     */
    private fun exportToPdf() {
        lifecycleScope.launch(Dispatchers.IO) {

            val transactions = db.transactionDao().getAllTransactions().first()   // Flow → List

            val pdfDocument = PdfDocument()
            val paint = Paint()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            var y = 40
            val startX = 40
            val rowHeight = 25

            // Column widths
            val colDate = 150
            val colType = 80
            val colAmount = 80
            val colNote = 200

            // ===== TITLE =====
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("Expense Tracker Report", startX.toFloat(), y.toFloat(), paint)

            y += 30

            // ===== HEADER ROW =====
            paint.textSize = 12f
            paint.isFakeBoldText = true

            fun drawCell(text: String, x: Int, y: Int, width: Int) {

                // Draw border only
                paint.style = Paint.Style.STROKE
                paint.color = Color.BLACK
                canvas.drawRect(
                    x.toFloat(),
                    (y - rowHeight).toFloat(),
                    (x + width).toFloat(),
                    y.toFloat(),
                    paint
                )

                // Draw text
                paint.style = Paint.Style.FILL
                paint.color = Color.BLACK
                canvas.drawText(
                    text, (x + 5).toFloat(), (y - 8).toFloat(), paint
                )
            }

            var x = startX
            drawCell("Date", x, y, colDate); x += colDate
            drawCell("Type", x, y, colType); x += colType
            drawCell("Amount", x, y, colAmount); x += colAmount
            drawCell("Note", x, y, colNote)

            y += rowHeight
            paint.isFakeBoldText = false

            // ===== DATA ROWS =====
            transactions.forEach { t ->

                // New page if needed
                if (y > 800) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40
                }

                x = startX
                drawCell(t.date, x, y, colDate); x += colDate
                drawCell(t.type, x, y, colType); x += colType
                drawCell("₹${t.amount}", x, y, colAmount); x += colAmount
                drawCell(t.desc ?: "", x, y, colNote)

                y += rowHeight
            }

            pdfDocument.finishPage(page)

            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Expense_Report_${System.currentTimeMillis()}.pdf"
            )

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "PDF table saved in Downloads", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}