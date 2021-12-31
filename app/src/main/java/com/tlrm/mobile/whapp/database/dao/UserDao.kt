package com.tlrm.mobile.whapp.database.dao

import androidx.room.*
import com.tlrm.mobile.whapp.database.entities.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM USERS where id = :id")
    fun getUserById(id: Int): UserEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: UserEntity)

    @Update
    fun updateAll(vararg users: UserEntity)

    @Query("SELECT * from USERS where user_name_mobile = :username COLLATE NOCASE")
    fun findByUserName(username: String): UserEntity
}