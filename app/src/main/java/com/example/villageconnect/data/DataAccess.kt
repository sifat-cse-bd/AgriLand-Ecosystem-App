package com.example.villageconnect.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import com.example.villageconnect.auth.Registration

object DataAccess {
    private var dbHelper: DBHelper? = null
    private var database: SQLiteDatabase? = null

    fun init(context: Context) {
        if (dbHelper == null) {
            dbHelper = DBHelper(context.applicationContext)
        }
    }

    private fun openReadableDatabase(context: Context): SQLiteDatabase {
        init(context)

        if (database == null || database?.isOpen == false) {
            database = dbHelper!!.readableDatabase
        }

        return database!!
    }

    private fun openWritableDatabase(context: Context): SQLiteDatabase {
        init(context)

        if (database == null || database?.isOpen == false) {
            database = dbHelper!!.writableDatabase
        }

        return database!!
    }

    // SELECT query
    fun executeQuery(
        context: Context,
        sql: String,
        args: Array<String>? = null
    ): Cursor? {
        return try {
            val db = openReadableDatabase(context)
            db.rawQuery(sql, args)
        } catch (ex: Exception) {
            Toast.makeText(context, "Query Error: ${ex.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    // INSERT, UPDATE, DELETE
    fun executeDMLQuery(
        context: Context,
        sql: String,
        args: Array<Any>? = null
    ): Boolean {
        return try {
            val db = openWritableDatabase(context)

            if (args == null) {
                db.execSQL(sql)
            } else {
                db.execSQL(sql, args)
            }

            true
        } catch (ex: Exception) {
            Toast.makeText(context, "DML Error: ${ex.message}", Toast.LENGTH_LONG).show()
            false
        }
    }

    // INSERT, UPDATE, DELETE with transaction
    fun executeDMLWithTransaction(
        context: Context,
        sql: String,
        args: Array<Any>? = null
    ): Boolean {
        val db = openWritableDatabase(context)
        db.beginTransaction()

        return try {
            if (args == null) {
                db.execSQL(sql)
            } else {
                db.execSQL(sql, args)
            }

            db.setTransactionSuccessful()
            true
        } catch (ex: Exception) {
            Toast.makeText(context, "Transaction Error: ${ex.message}", Toast.LENGTH_LONG).show()
            false
        } finally {
            db.endTransaction()
        }
    }

    // Multiple DML with transaction and rollback
    fun executeMultipleDMLWithTransaction(
        context: Context,
        queries: List<Pair<String, Array<Any>?>>
    ): Boolean {
        val db = openWritableDatabase(context)
        db.beginTransaction()

        return try {
            for (query in queries) {
                val sql = query.first
                val args = query.second

                if (args == null) {
                    db.execSQL(sql)
                } else {
                    db.execSQL(sql, args)
                }
            }

            db.setTransactionSuccessful()
            true

        } catch (ex: Exception) {
            Toast.makeText(context, "Rollback Done: ${ex.message}", Toast.LENGTH_LONG).show()
            false

        } finally {
            db.endTransaction()
        }
    }

    // Count query
    fun executeScalarInt(
        context: Context,
        sql: String,
        args: Array<String>? = null
    ): Int {
        var result = 0
        val cursor = executeQuery(context, sql, args)

        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getInt(0)
            }
        } catch (ex: Exception) {
            Toast.makeText(context, "Scalar Error: ${ex.message}", Toast.LENGTH_LONG).show()
        } finally {
            cursor?.close()
        }

        return result
    }

    // Close database
    fun closeDatabase() {
        if (database != null && database!!.isOpen) {
            database!!.close()
        }

        database = null
        dbHelper = null
    }
}