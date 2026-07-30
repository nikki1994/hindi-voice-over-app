package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VoiceoverEntity::class], version = 1, exportSchema = false)
abstract class VoiceoverDatabase : RoomDatabase() {
    abstract fun voiceoverDao(): VoiceoverDao

    companion object {
        @Volatile
        private var INSTANCE: VoiceoverDatabase? = null

        fun getDatabase(context: Context): VoiceoverDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoiceoverDatabase::class.java,
                    "hindi_voiceover_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
