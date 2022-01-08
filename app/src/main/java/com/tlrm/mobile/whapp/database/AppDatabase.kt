package com.tlrm.mobile.whapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tlrm.mobile.whapp.database.dao.PalleteDao
import com.tlrm.mobile.whapp.database.dao.PickListDao
import com.tlrm.mobile.whapp.database.dao.RequestDao
import com.tlrm.mobile.whapp.database.dao.UserDao
import com.tlrm.mobile.whapp.database.entities.*

@Database(
    entities = [UserEntity::class, PickListEntity::class, PallateEntity::class, RequestEntity::class,
        RequestEmptyItemEntity::class, RequestDocketItemEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun pickListDao(): PickListDao
    abstract fun paletteDao(): PalleteDao
    abstract fun requestDao(): RequestDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE =
                        Room.databaseBuilder(context, AppDatabase::class.java, "TNRMS_LOCAL_DB")
                            .build()
                }
            }
            return INSTANCE!!
        }
    }
}
