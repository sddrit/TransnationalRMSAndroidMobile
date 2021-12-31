package com.tlrm.mobile.whapp.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tlrm.mobile.whapp.database.core.StringArrayConverter

@Entity(tableName = "Users")
data class UserEntity (
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "user_name_mobile") val userNameMobile: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "active") val active: Boolean,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    @ColumnInfo(name = "password_salt") val passwordSalt: String,
    @ColumnInfo(name = "roles")
    @field:TypeConverters(StringArrayConverter::class)
    val roles: ArrayList<String>
)

