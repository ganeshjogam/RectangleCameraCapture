package com.example.rectanglecameracapture.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val NUMBER_OF_THREADS = 4;
val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS)
@Database(entities = arrayOf(Photo::class),version = 1)
abstract class PhotoDatabase: RoomDatabase() {
    abstract fun photoDao(): PhotoDao

    companion object {
        private const val DB_NAME="photos.db"

        fun newInstance(context: Context):PhotoDatabase{
            return Room.databaseBuilder(context,PhotoDatabase::class.java, DB_NAME).build()
        }
    }
}