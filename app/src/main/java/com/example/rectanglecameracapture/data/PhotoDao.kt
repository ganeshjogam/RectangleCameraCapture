package com.example.rectanglecameracapture.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo")
    fun getAll(): LiveData<List<Photo>>

    @Query("SELECT * FROM photo where id=:id LIMIT 1")
    fun findPhotoById(id: Int): LiveData<Photo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(photos: List<Photo>)

    @Delete
    fun delete(photo: Photo)
}