package com.example.villageconnect.models

import android.content.Context
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess

class AutoIdGenerator{

    val  dataAccess = DataAccess

    fun farmerHireIdGenerator(context: Context):Int
    {
        val sql = "SELECT MAX(${DBHelper.COL_ID}) FROM ${DBHelper.TABLE_HIRE_REQUESTS}"
        val cursor = dataAccess.executeQuery(context, sql)
        var nextId=1
        try{
            if(cursor != null && cursor.moveToFirst()) {
                val currentId = cursor.getInt(0)
                nextId = currentId + 1
            }

        }finally {
            cursor?.close()
        }
       return nextId
    }



}