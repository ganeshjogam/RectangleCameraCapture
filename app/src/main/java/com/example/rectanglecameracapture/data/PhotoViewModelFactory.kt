package com.example.rectanglecameracapture.data

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rectanglecameracapture.PhotoViewModel

class PhotoViewModelFactory(val application: Application): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PhotoViewModel(application) as T
    }

}