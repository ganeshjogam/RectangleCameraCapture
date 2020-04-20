package com.example.rectanglecameracapture.data

import androidx.lifecycle.LiveData

class PhotoRepository(val photoDao: PhotoDao) {
    fun getPhotos(): LiveData<List<Photo>> {
        return photoDao.getAll()
    }

    fun getPhotoById(id: Int): LiveData<Photo> {
        return photoDao.findPhotoById(id)
    }

    fun savePhotos(photos: List<Photo>) {
        databaseWriteExecutor.execute {
            photoDao.insert(photos)
        }
    }
}