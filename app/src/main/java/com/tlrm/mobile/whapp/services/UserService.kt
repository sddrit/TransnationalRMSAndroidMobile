package com.tlrm.mobile.whapp.services

import android.util.Log
import com.tlrm.mobile.whapp.api.LoginHistory
import com.tlrm.mobile.whapp.api.UserApiService
import com.tlrm.mobile.whapp.database.dao.UserDao
import com.tlrm.mobile.whapp.database.entities.UserEntity
import com.tlrm.mobile.whapp.util.Password
import com.tlrm.mobile.whapp.util.exceptions.ServiceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserService (private val userApiService: UserApiService,
    private val userDao: UserDao) {

    fun getUserByUserName(username: String): UserEntity? {
        return userDao.findByUserName(username)
    }

    suspend fun login(username: String, password: String): UserEntity {
        var user: UserEntity? = null;

        withContext(Dispatchers.IO) {
            user = getUserByUserName(username)
        }

        if (user == null) {
            throw ServiceException("Invalid credentials, Please try again")
        }

        if (!user!!.active) {
            throw ServiceException("Sorry your account is deactive, Please contact administrator")
        }

        var isValidPassword =
            Password.IsPasswordMatch(user!!.passwordHash, user!!.passwordSalt, password)

        if (!isValidPassword) {
            throw ServiceException("Invalid credentials, Please try again")
        }

        return user!!;
    }

    suspend fun addLoginHistory(userId: Int, loginDate: String, hostName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val call = userApiService.addUserLoginHistory(LoginHistory(userId, loginDate, hostName))
            val response = call.execute()
            if (!response.isSuccessful) {
                return@withContext false;
            }
            return@withContext true;
        }
    }

    suspend fun refresh() {
        withContext(Dispatchers.IO) {

            var getUsersCall = userApiService.getUsers()
            var response = getUsersCall.execute()

            if (!response.isSuccessful) {
                Log.e("TNRMS-MOBILE",
                    "Unable to get users from api")
                return@withContext
            }

            var users = response.body()

            if (users == null) {
                Log.e("TNRMS-MOBILE",
                    "Empty list return from get user api endpoint")
                return@withContext
            }

            for (user in users) {
                try {
                    userDao.insertAll(
                        UserEntity(
                        user.id, user.userName, user.userNameMobile,
                        user.fullName, user.active, user.passwordHash,
                        user.passwordSalt, user.roles
                    ))
                } catch (e: Exception) {
                    Log.e("TNRMS-MOBILE",
                        "Unable to insert user entity to local database", e)
                }
            }
        }
    }
}