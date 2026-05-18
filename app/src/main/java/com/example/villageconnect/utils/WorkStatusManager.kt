package com.example.villageconnect.utils

import android.content.Context
import android.util.Log
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Manages automatic status updates for hire requests and work
 */
object WorkStatusManager {

    /**
     * Auto-expire pending requests that passed 7 AM deadline
     */
    fun handleExpiredRequests(context: Context) {
        try {
            val now = LocalDateTime.now()

            // Find all pending requests where expires_at has passed
            val sql = """
                SELECT ${DBHelper.COL_ID} FROM ${DBHelper.TABLE_HIRE_REQUESTS}
                WHERE ${DBHelper.COL_REQUEST_STATUS} = '${DBHelper.STATUS_PENDING}'
                  AND expires_at < ?
            """

            val cursor = DataAccess.executeQuery(context, sql, arrayOf(now.toString()))
            val expiredIds = mutableListOf<Int>()

            cursor?.use {
                while (it.moveToNext()) {
                    expiredIds.add(it.getInt(0))
                }
            }

            // Update all expired requests to Rejected
            for (id in expiredIds) {
                val updateSql = """
                    UPDATE ${DBHelper.TABLE_HIRE_REQUESTS}
                    SET ${DBHelper.COL_REQUEST_STATUS} = '${DBHelper.STATUS_REJECTED}', ${DBHelper.COL_UPDATED_AT} = CURRENT_TIMESTAMP
                    WHERE ${DBHelper.COL_ID} = ?
                """
                DataAccess.executeDMLQuery(context, updateSql, arrayOf(id))
                Log.d("WorkStatusManager", "Expired request $id")
            }
        } catch (e: Exception) {
            Log.e("WorkStatusManager", "Error handling expired requests: ${e.message}")
        }
    }

    /**
     * Auto-mark work as incomplete if farmer didn't start by next day 7 AM
     */
    fun handleIncompleteWork(context: Context) {
        try {
            val today = LocalDate.now()

            // Find all hire_work records that are still Pending (not started)
            // but work_date was yesterday or earlier
            val sql = """
                SELECT hw.${DBHelper.COL_ID} FROM ${DBHelper.TABLE_HIRE_WORK} hw
                JOIN ${DBHelper.TABLE_HIRE_REQUESTS} hr ON hw.${DBHelper.COL_HIRE_REQUEST_ID} = hr.${DBHelper.COL_ID}
                WHERE hw.${DBHelper.COL_WORK_STATUS} = '${DBHelper.STATUS_PENDING}'
                  AND hr.${DBHelper.COL_WORK_DATE} < ?
            """

            val cursor = DataAccess.executeQuery(context, sql, arrayOf(today.toString()))
            val incompleteIds = mutableListOf<Int>()

            cursor?.use {
                while (it.moveToNext()) {
                    incompleteIds.add(it.getInt(0))
                }
            }

            // Mark these as incomplete (could add new status or use special marker)
            for (id in incompleteIds) {
                val updateSql = """
                    UPDATE ${DBHelper.TABLE_HIRE_WORK}
                    SET ${DBHelper.COL_WORK_STATUS} = 'Incomplete', ${DBHelper.COL_UPDATED_AT} = CURRENT_TIMESTAMP
                    WHERE ${DBHelper.COL_ID} = ?
                """
                DataAccess.executeDMLQuery(context, updateSql, arrayOf(id))
                Log.d("WorkStatusManager", "Marked work $id as incomplete")
            }
        } catch (e: Exception) {
            Log.e("WorkStatusManager", "Error handling incomplete work: ${e.message}")
        }
    }

    /**
     * Called periodically to check and update statuses
     */
    fun checkAndUpdateStatuses(context: Context) {
        handleExpiredRequests(context)
        handleIncompleteWork(context)
    }
}

