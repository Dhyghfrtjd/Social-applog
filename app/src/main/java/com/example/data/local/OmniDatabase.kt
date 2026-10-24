package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.OfflineDraft
import com.example.model.PlatformMetric
import com.example.model.SocialNotification
import com.example.model.VaultItem

@Database(
    entities = [
        SocialNotification::class,
        VaultItem::class,
        OfflineDraft::class,
        PlatformMetric::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OmniDatabase : RoomDatabase() {
    abstract fun omniDao(): OmniDao

    companion object {
        @Volatile
        private var INSTANCE: OmniDatabase? = null

        fun getDatabase(context: Context): OmniDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OmniDatabase::class.java,
                    "omnisocial_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
