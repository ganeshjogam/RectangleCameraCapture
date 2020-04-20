package com.example.rectanglecameracapture

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.hardware.Camera.ShutterCallback
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_photo.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.String
import java.text.SimpleDateFormat
import java.util.*


class PhotoFragment : Fragment(), SurfaceHolder.Callback {
    var camera: Camera? = null
    var previewing = false
    var previewSizeOptimal: Camera.Size? = null
    lateinit var surfaceView: SurfaceView
    var surfaceHolder: SurfaceHolder? = null
    var photoFragmentAction: PhotoFragmentAction? = null
    val REQUEST_GALLERY_PHOTO = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_photo, container, false)

        surfaceView = view.findViewById(R.id.camera_preview_surface)
        surfaceHolder = surfaceView.holder
        surfaceHolder?.addCallback(this)
        view.findViewById<Button>(R.id.make_photo_button)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    if (camera != null) {
                        camera!!.takePicture(
                            myShutterCallback,
                            myPictureCallback_RAW, myPictureCallback_JPG
                        )
                    }
                }
            })

        view.findViewById<LinearLayout>(R.id.res_photo_layout).setOnClickListener {
            dispatchGalleryIntent()
        }

        return view
    }

    private fun dispatchGalleryIntent() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is PhotoFragment.PhotoFragmentAction) {
            photoFragmentAction = activity as PhotoFragmentAction
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        surfaceHolder = holder
        if (previewing) {
            camera?.stopPreview()
            previewing = false
        }

        if (camera != null) {
            try {
                val parameters = camera!!.parameters
                val previewSizes = parameters.supportedPreviewSizes

                previewSizeOptimal = getOptimalPreviewSize(
                    previewSizes,
                    parameters.pictureSize.width,
                    parameters.pictureSize.height
                )
                previewSizeOptimal.let { size ->
                    if (size != null) {
                        parameters.setPreviewSize(size.width, size.height)
                    }
                }

                if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                }

                if (parameters.supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.focusMode = Camera.Parameters.FLASH_MODE_AUTO
                }

                camera?.parameters = parameters

                val display =
                    (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                if (display.rotation == Surface.ROTATION_0) {
                    camera?.setDisplayOrientation(90)
                } else if (display.rotation == Surface.ROTATION_270) {
                    camera?.setDisplayOrientation(180)
                }

                val x1: Int = preview_layout.width
                val y1: Int = preview_layout.height

                val x2: Int = border_camera.width
                val y2: Int = border_camera.height

                val info = """
                Preview width:${String.valueOf(x1)}
                Preview height:${String.valueOf(y1)}
                Border width:${String.valueOf(x2)}
                Border height:${String.valueOf(y2)}
                """.trimIndent()

                camera?.setPreviewDisplay(holder)
                camera?.startPreview()
                previewing = true
                camera?.autoFocus(null)

            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        camera?.stopPreview()
        camera?.release()
        camera = null
        previewing = false
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        camera = Camera.open(0);
    }

    fun getOptimalPreviewSize(sizes: List<Camera.Size>, width: Int, height: Int): Camera.Size? {
        val ASPECT_TOLERANCE = 0.1
        val targetRatio: Double = width.toDouble() / height
        if (sizes == null) return null

        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE

        val targetHeight: Int = height

        // Try to find an size match aspect ratio and size

        // Try to find an size match aspect ratio and size
        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - targetHeight).toDouble()
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - targetHeight).toDouble()
                }
            }
        }
        return optimalSize
    }

    fun createImageFile(bitmap: Bitmap) {
        val path: File = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )

        val timeStamp: kotlin.String = SimpleDateFormat("MMdd_HHmmssSSS").format(Date())
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
            Toast.makeText(context, file.getName(), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing $file", e)
        }
    }

    var myShutterCallback = ShutterCallback {

    }

    //leave it empty
    var myPictureCallback_RAW =
        PictureCallback { data, camera ->

        }

    //we need only JPG
    var myPictureCallback_JPG =
        PictureCallback { data, camera ->
            val bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.size)
            var croppedBitmap: Bitmap? = null
            val display =
                (context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            if (display.rotation == Surface.ROTATION_0) {

                //rotate bitmap, because camera sensor usually in landscape mode
                val matrix = Matrix()
                matrix.postRotate(90f)
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmapPicture, 0, 0,
                    bitmapPicture.width, bitmapPicture.height, matrix, true
                )
                //save file
                //createImageFile(rotatedBitmap)

                //calculate aspect ratio
                val koefX =
                    rotatedBitmap.width.toFloat() / preview_layout.getWidth().toFloat()
                val koefY: Float =
                    rotatedBitmap.height.toFloat() / preview_layout.getHeight().toFloat()

                //get viewfinder border size and position on the screen
                val x1: Int = border_camera.getLeft()
                val y1: Int = border_camera.getTop()
                val x2: Int = border_camera.getWidth()
                val y2: Int = border_camera.getHeight()

                //calculate position and size for cropping
                val cropStartX = Math.round(x1 * koefX)
                val cropStartY = Math.round(y1 * koefY)
                val cropWidthX = Math.round(x2 * koefX)
                val cropHeightY = Math.round(y2 * koefY)

                //check limits and make crop
                croppedBitmap = if (cropStartX + cropWidthX <= rotatedBitmap.width &&
                    cropStartY + cropHeightY <= rotatedBitmap.height
                ) {
                    Bitmap.createBitmap(
                        rotatedBitmap, cropStartX,
                        cropStartY, cropWidthX, cropHeightY
                    )
                } else {
                    null
                }

                //save result
                croppedBitmap?.let {
                    //pass to another fragment
                    if (croppedBitmap != null) {
                        photoFragmentAction?.onCropBitmap(croppedBitmap)
                    }
                    //createImageFile(it)
                }
            } else if (display.rotation == Surface.ROTATION_270) {
                // for Landscape mode
            }

            camera?.startPreview()
        }

    interface PhotoFragmentAction {
        fun onCropBitmap(cropImage: Bitmap?)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY_PHOTO) {
            val uri: Uri? = data?.data

        }
    }

    fun getRealPathFromUri(contentUri: Uri?): kotlin.String? {
        var cursor: Cursor? = null
        return try {
            val proj =
                arrayOf(MediaStore.Images.Media.DATA)
            cursor = activity!!.getContentResolver().query(contentUri!!, proj, null, null, null)
            assert(cursor != null)
            val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }


}
