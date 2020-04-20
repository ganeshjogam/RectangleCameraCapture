package com.example.rectanglecameracapture

import android.graphics.Bitmap
import android.location.Location
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.rectanglecameracapture.data.Photo
import com.example.rectanglecameracapture.data.PhotoViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class ImageFragment : Fragment() {
    private var bitmap: Bitmap? = null
    lateinit var resPhoto: ImageView
    lateinit var resPhotoSize: TextView
    var imageFragmentListener: ImageFragmentListener? = null
    lateinit var photoViewModel: PhotoViewModel
    lateinit var currentLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        photoViewModel = ViewModelProvider(
            requireActivity(),
            PhotoViewModelFactory(requireActivity().application)
        )
            .get(PhotoViewModel::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is ImageFragmentListener) {
            imageFragmentListener = activity as ImageFragmentListener
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_image, container, false)

        resPhoto = view.findViewById(R.id.res_photo)
        if (bitmap != null) {
            resPhoto.setImageBitmap(bitmap)

            val info = """
                image with:${bitmap!!.width}
                image height:${bitmap!!.height}
                """.trimIndent()

            resPhotoSize.text = info
        }

        view.findViewById<Button>(R.id.save_image).setOnClickListener {
            createImageFile(bitmap!!)
        }

        view.findViewById<Button>(R.id.retake).setOnClickListener {
            imageFragmentListener?.onRetake()
        }

        return view
    }

    fun imageSetupFragment(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    interface ImageFragmentListener {
        fun onImageSave()
        fun onRetake()
    }

    fun createImageFile(bitmap: Bitmap) {
        val path: File = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )

        val timeStamp: String = SimpleDateFormat("MMdd_HHmmssSSS").format(Date())
        val imageFileName = "region_$timeStamp.jpg"
        val file = File(path, imageFileName)

        try {
            // Make sure the Pictures directory exists.
            if (path.mkdirs()) {
                Toast.makeText(context, "Not exist :" + path.getName(), Toast.LENGTH_SHORT).show()
            }
            val os: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
            Log.i("ExternalStorage", "Writed " + path + file.getName())
            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(
                context, arrayOf(file.toString()), null
            ) { path, uri ->
                Log.i("ExternalStorage", "Scanned $path:")
                Log.i("ExternalStorage", "-> uri=$uri")
            }
            photoViewModel.savePhoto(
                Photo(
                    0,
                    file.name,
                    timeStamp,
                    currentLocation.latitude,
                    currentLocation.longitude,
                    file.absolutePath
                )
            )
            imageFragmentListener?.onImageSave()
            Toast.makeText(context, file.getName(), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing $file", e)
        }
    }

}
