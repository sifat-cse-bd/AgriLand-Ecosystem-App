package com.example.villageconnect.landowner.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.villageconnect.R
import com.example.villageconnect.data.DBHelper
import com.example.villageconnect.data.DataAccess
import com.example.villageconnect.landowner.adapters.InventoryAdapter
import com.example.villageconnect.landowner.models.InventoryItem
import com.example.villageconnect.utils.SessionManager

class InventoryList : Fragment(R.layout.fragment_inventory_list) {

    private lateinit var rvInventory: RecyclerView

    private var landownerId = -1
    private var villageName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvInventory = view.findViewById(R.id.rvInventory)
        rvInventory.layoutManager = LinearLayoutManager(requireContext())

        landownerId = SessionManager(requireContext()).getUserId()
        villageName = getLandownerVillage()

        loadInventory()
    }

    private fun getLandownerVillage(): String? {
        val sql = """
            SELECT ${DBHelper.COL_VILLAGE_NAME}
            FROM ${DBHelper.TABLE_USERS}
            WHERE ${DBHelper.COL_ID} = ?
        """

        val cursor = DataAccess.executeQuery(requireContext(), sql, arrayOf(landownerId.toString()))

        var village: String? = null

        cursor?.use {
            if (it.moveToFirst()) village = it.getString(0)
        }

        return village
    }

    private fun loadInventory() {
        val items = mutableListOf<InventoryItem>()

        val sql = """
            SELECT i.${DBHelper.COL_ID},
                   i.${DBHelper.COL_MERCHANT_ID},
                   i.${DBHelper.COL_ITEM_NAME},
                   i.${DBHelper.COL_CATEGORY},
                   i.${DBHelper.COL_PRICE},
                   i.${DBHelper.COL_STOCK_QTY}
            FROM ${DBHelper.TABLE_INVENTORY} i
            INNER JOIN ${DBHelper.TABLE_USERS} u
            ON i.${DBHelper.COL_MERCHANT_ID} = u.${DBHelper.COL_ID}
            WHERE u.${DBHelper.COL_VILLAGE_NAME} = ?
            AND u.${DBHelper.COL_ROLE} = ?
        """

        val cursor = DataAccess.executeQuery(
            requireContext(),
            sql,
            arrayOf(villageName ?: "", DBHelper.ROLE_MERCHANT)
        )

        cursor?.use {
            while (it.moveToNext()) {
                items.add(
                    InventoryItem(
                        id = it.getInt(0),
                        merchantId = it.getInt(1),
                        itemName = it.getString(2),
                        category = it.getString(3),
                        price = it.getDouble(4),
                        stockQty = it.getInt(5)
                    )
                )
            }
        }

        rvInventory.adapter = InventoryAdapter(items) { item ->
            showOrderDialog(item)
        }
    }

    private fun showOrderDialog(item: InventoryItem) {
        val input = EditText(requireContext())
        input.hint = "Enter quantity"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(requireContext())
            .setTitle("Order ${item.itemName}")
            .setMessage("Price: ৳${item.price} | Stock: ${item.stockQty}")
            .setView(input)
            .setPositiveButton("Order") { _, _ ->
                val quantity = input.text.toString().toIntOrNull() ?: 0

                if (quantity <= 0) {
                    Toast.makeText(requireContext(), "Invalid quantity", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (quantity > item.stockQty) {
                    Toast.makeText(requireContext(), "Not enough stock", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                createOrder(item, quantity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createOrder(item: InventoryItem, quantity: Int) {
        val totalPrice = item.price * quantity

        val queries = listOf(
            Pair(
                """
                INSERT INTO ${DBHelper.TABLE_INVENTORY_ORDERS}
                (${DBHelper.COL_LANDOWNER_ID}, ${DBHelper.COL_MERCHANT_ID}, ${DBHelper.COL_INVENTORY_ID}, ${DBHelper.COL_QUANTITY}, ${DBHelper.COL_TOTAL_PRICE}, ${DBHelper.COL_STATUS})
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                arrayOf<Any>(landownerId, item.merchantId, item.id, quantity, totalPrice, DBHelper.STATUS_PENDING)
            ),
            Pair(
                """
                UPDATE ${DBHelper.TABLE_INVENTORY}
                SET ${DBHelper.COL_STOCK_QTY} = ${DBHelper.COL_STOCK_QTY} - ?
                WHERE ${DBHelper.COL_ID} = ?
                """,
                arrayOf<Any>(quantity, item.id)
            )
        )

        val result = DataAccess.executeMultipleDMLWithTransaction(requireContext(), queries)

        if (result) {
            Toast.makeText(requireContext(), "Order placed", Toast.LENGTH_SHORT).show()
            loadInventory()
        } else {
            Toast.makeText(requireContext(), "Order failed", Toast.LENGTH_SHORT).show()
        }
    }
}