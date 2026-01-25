package com.example.expensetracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.dao.CategoryDao
import com.example.expensetracker.data.dao.TransactionDao
import com.example.expensetracker.data.entity.CategoryEntity
import com.example.expensetracker.data.entity.TransactionEntity

@Database(entities = [CategoryEntity::class, TransactionEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "expense_db"
                ).fallbackToDestructiveMigration()/*addMigrations(MIGRATION_1_2)*/.build()

            }
            return INSTANCE!!
        }

        private val MIGRATION_1_2 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE categories ADD COLUMN iconRes INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

    }
}