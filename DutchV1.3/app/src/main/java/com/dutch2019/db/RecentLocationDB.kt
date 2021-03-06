package com.dutch2019.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dutch2019.model.LocationDataDB
import com.dutch2019.model.LocationInfo

@Database(entities = [LocationDataDB::class], version=1)
@TypeConverters(Converters::class)
public abstract class RecentLocationDB : RoomDatabase() {
    abstract fun locationInfoDao(): LocationInfoDao

    companion object {

        private  var instance: RecentLocationDB? = null

        fun getDatabase(context: Context): RecentLocationDB? {
            if (instance == null) {
                synchronized(RecentLocationDB::class.java) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context.applicationContext,
                            RecentLocationDB::class.java, "recentlocation-db"
                        ).build()
                    }
                }
            }
            return instance
        }
    }
}