package com.tlrm.mobile.whapp.services

import android.content.Context
import android.content.SharedPreferences
import com.tlrm.mobile.whapp.database.entities.UserEntity

class SessionService(private val context: Context) {

    private val USER_PREFERENCES: String = "USER_PREFERENCES"
    private val PRIVATE_MODE: Int = 0

    fun clearUser() {
        val preferences = getUserPreferences()
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

     fun setUser(user: UserEntity) {
         val preferences = getUserPreferences()
         val editor = preferences.edit()
         editor.putInt("Id", user.id)
         editor.putString("UserName", user.userName)
         editor.putString("UserNameMobile", user.userNameMobile)
         editor.putString("FullName", user.fullName)
         editor.putString("Roles", user.roles.joinToString())
         editor.apply()
     }

    fun getUser() :UserEntity {
        val preferences = getUserPreferences()
        return UserEntity(preferences.getInt("Id", 0),
            preferences.getString("UserName", null)!!,
            preferences.getString("UserNameMobile", null)!!,
            preferences.getString("FullName", null)!!,
            true,
            "",
            "",
            preferences.getString("Roles", null)!!.split(",")
                .toCollection(ArrayList()))
    }

    private fun getUserPreferences(): SharedPreferences {
        return context.getSharedPreferences(USER_PREFERENCES, PRIVATE_MODE)
    }

}