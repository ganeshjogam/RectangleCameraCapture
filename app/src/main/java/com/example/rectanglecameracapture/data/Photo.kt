package com.example.rectanglecameracapture.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val path: String
)