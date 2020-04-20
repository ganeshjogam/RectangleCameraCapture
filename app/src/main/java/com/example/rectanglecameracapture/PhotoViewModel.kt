package com.example.rectanglecameracapture

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.rectanglecameracapture.data.Photo
import com.example.rectanglecameracapture.data.PhotoDatabase
import com.example.rectanglecameracapture.data.PhotoRepository

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    var repository: PhotoRepository

    init {
        repository = PhotoRepository(PhotoDatabase.newInstance(application.applicationContext).photoDao())
    }

    val photoList: LiveData<List<Photo>> = repository.getPhotos()

    fun savePhoto(photo: Photo) {
        repository.savePhotos(arrayListOf(photo))
    }
}