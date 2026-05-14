package com.example.villageconnect.utils

import android.content.Context

class SessionManager(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("village_connect_session", Context.MODE_PRIVATE)

    fun saveUserSession(userId: Int, role: String) {
        sharedPreferences.edit()
            .putInt("user_id", userId)
            .putString("role", role)
            .putBoolean("is_logged_in", true)
            .apply()
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1)
    }

    fun getUserRole(): String? {
        return sharedPreferences.getString("role", null)
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
}