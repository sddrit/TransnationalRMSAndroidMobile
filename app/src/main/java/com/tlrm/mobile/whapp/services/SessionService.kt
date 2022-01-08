package com.tlrm.mobile.whapp.services

import android.content.Context
import android.content.SharedPreferences
import com.tlrm.mobile.whapp.database.entities.UserEntity

data class DeviceInfo(val code: String, val description: String, val active: Boolean)
data class StorageFieldDefinition(val cartonLength: Int, val locationLength: Int)

class SessionService(private val context: Context) {

    private val USER_PREFERENCES: String = "USER_PREFERENCES"
    private val DEVICE_PREFERENCES: String = "DEVICE_PREFERENCES"
    private val STORAGE_FIELD_PREFERENCES: String = "STORAGE_FIELD_PREFERENCES"
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

    fun getUser(): UserEntity {
        val preferences = getUserPreferences()
        return UserEntity(
            preferences.getInt("Id", 0),
            preferences.getString("UserName", null)!!,
            preferences.getString("UserNameMobile", null)!!,
            preferences.getString("FullName", null)!!,
            true,
            "",
            "",
            preferences.getString("Roles", null)!!.split(",")
                .toCollection(ArrayList())
        )
    }

    fun setDevice(code: String, description: String, active: Boolean) {
        val preferences = getDevicePreferences()
        val editor = preferences.edit()
        editor.putString("Code", code)
        editor.putString("Description", description)
        editor.putBoolean("Active", active)
        editor.apply()
    }

    fun getDevice(): DeviceInfo? {
        val preferences = getDevicePreferences()

        if (preferences.all.isEmpty()) {
            return null
        }
        return DeviceInfo(
            preferences.getString("Code", null)!!,
            preferences.getString("Description", null)!!,
            preferences.getBoolean("Active", false)
        )
    }

    fun setStorageFieldDefinition(cartonLength: Int, locationLength: Int) {
        val preferences = getStorageFieldDefinitionPreferences()
        val editor = preferences.edit()
        editor.putInt("CartonLength", cartonLength)
        editor.putInt("LocationLength", locationLength)
        editor.apply()
    }

    fun getStorageFieldDefinition(): StorageFieldDefinition? {
        val preferences = getStorageFieldDefinitionPreferences()
        if (preferences.all.isEmpty()) {
            return null
        }
        return StorageFieldDefinition(
            preferences.getInt("CartonLength", 0),
            preferences.getInt("LocationLength", 0)
        )
    }

    private fun getDevicePreferences(): SharedPreferences {
        return context.getSharedPreferences(DEVICE_PREFERENCES, PRIVATE_MODE)
    }

    private fun getUserPreferences(): SharedPreferences {
        return context.getSharedPreferences(USER_PREFERENCES, PRIVATE_MODE)
    }

    private fun getStorageFieldDefinitionPreferences(): SharedPreferences {
        return context.getSharedPreferences(STORAGE_FIELD_PREFERENCES, PRIVATE_MODE)
    }

}