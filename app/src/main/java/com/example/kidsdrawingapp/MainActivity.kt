package com.example.kidsdrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.example.kidsdrawingapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    //Variables
    private lateinit var binding: ActivityMainBinding
    private var globalButtonCurrentPaint: ImageButton? = null
    var customProgressDialog: Dialog? = null


    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>


    private val galleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            binding.ivCustomImage.setImageURI(result.data?.data)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Multiple permissions
        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {


                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        isReadPermissionGranted = true
                    } else if (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        isWritePermissionGranted = true
                    }


                    //Use special intent for media store
                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    //pick image from gallery
                    galleryLauncher.launch(pickIntent)

                } else {
                    Toast.makeText(this, "Permission $permissionName Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //Scale brush with default size of 20
        binding.drawingView.scaleBrush(20f)

        val llPaintColors = binding.llPaintColors
        globalButtonCurrentPaint = llPaintColors[1] as ImageButton
        globalButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )
        binding.ibImage.setOnClickListener {
            requestPermissions()
        }

        binding.ibBrush.setOnClickListener {
            showBrushDialog()
        }

        binding.ibEraser.setOnClickListener {
            showEraserDialog()
            binding.drawingView.setColor("#ffffff")
        }

        binding.ibUndo.setOnClickListener {
            binding.drawingView.onClickUndo()
        }

        binding.ibSave.setOnClickListener {

            if (isReadStorageAllowed()) {
                showProgressDialog()

                //Use that for apply coroutines in
                lifecycleScope.launch {
                    saveBitmapFile(getBitmapFromView(binding.flContainer))
                }

            }

        }

    }

    //Show rationale dialog for displaying why we need permission
    private fun showRationaleDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }


    //Show brush dialog to select brush size
    private fun showBrushDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn: ImageButton = brushDialog.findViewById(R.id.ibSmallBrush)
        val mediumBtn: ImageButton = brushDialog.findViewById(R.id.ibMediumBrush)
        val largeBtn: ImageButton = brushDialog.findViewById(R.id.ibLargeBrush)
        smallBtn.setOnClickListener {
            binding.drawingView.scaleBrush(10f)
            brushDialog.dismiss()
        }
        mediumBtn.setOnClickListener {
            binding.drawingView.scaleBrush(20f)
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener {
            binding.drawingView.scaleBrush(30f)
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    //Show brush dialog to select brush size
    private fun showEraserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_eraser_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn: ImageButton = brushDialog.findViewById(R.id.ibSmallBrush)
        val mediumBtn: ImageButton = brushDialog.findViewById(R.id.ibMediumBrush)
        val largeBtn: ImageButton = brushDialog.findViewById(R.id.ibLargeBrush)
        smallBtn.setOnClickListener {
            binding.drawingView.scaleBrush(10f)
            brushDialog.dismiss()
        }
        mediumBtn.setOnClickListener {
            binding.drawingView.scaleBrush(20f)
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener {
            binding.drawingView.scaleBrush(100f)
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    //Chose color from pallet
    fun paintClicked(view: View) {
        if (view != globalButtonCurrentPaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            binding.drawingView.setColor(colorTag)

            imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))

            globalButtonCurrentPaint?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_normal))

            globalButtonCurrentPaint = view

        }
    }

    //Check for readStorage ALLOWED
    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    //Request needed permissions
    private fun requestPermissions() {


        val isReadPermission = ContextCompat.checkSelfPermission(this,android.Manifest.permission
            .READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        val isWritePermission = ContextCompat.checkSelfPermission(this,android.Manifest.permission
            .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        //Set minimum SDK version into greater or equal to version Q(29)
        val minSdkLevel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        isReadPermissionGranted = isReadPermission
        isWritePermissionGranted = isWritePermission || minSdkLevel

        val permissionRequest =



        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showRationaleDialog("Kids Drawing App", "Kids Drawing App needs to Access Your External Storage")
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            showRationaleDialog(
                "Kids Drawing App", "Kids Drawing App needs Access to Your External Storage for " +
                        "saving your drawing"
            )
        } else {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE      // if uncomment that permission, app does`t quite
//                    automatically from gallery and opens 2 gallery's at same time
                )
            )
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        //Bitmap with clean layer
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        //Bitmap with clean and background layer
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(resources.getColor(R.color.white))
        }
        //Bitmap with clean, background and view layers
        view.draw(canvas)

        //Return sandwich bitmap
        return returnedBitmap
    }

    private suspend fun saveBitmapFile(gBitmap: Bitmap?): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if (gBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream() // Creates a new byte array output stream.
                    // The buffer capacity is initially 32 bytes, though its size increases if necessary.
                    gBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    /**
                     * Write a compressed version of the bitmap to the specified outputstream.
                     * If this returns true, the bitmap can be reconstructed by passing a
                     * corresponding inputstream to BitmapFactory.decodeStream(). Note: not
                     * all Formats support all bitmap configs directly, so it is possible that
                     * the returned bitmap from BitmapFactory could be in a different bitdepth,
                     * and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque
                     * pixels).
                     *
                     * @param format   The format of the compressed image
                     * @param quality  Hint to the compressor, 0-100. 0 meaning compress for
                     *                 small size, 100 meaning compress for max quality. Some
                     *                 formats, like PNG which is lossless, will ignore the
                     *                 quality setting
                     * @param stream   The outputstream to write the compressed data.
                     * @return true if successfully compressed to the specified stream.
                     */

                    //Create file
                    val file = File(
                        externalCacheDir?.absoluteFile.toString() + File.separator + "KidDrawingApp_" +
                                System.currentTimeMillis() / 1000 + ".png"
                    )
                    // Here the Environment : Provides access to environment variables.
                    // getExternalStorageDirectory : returns the primary shared/external storage directory.
                    // absoluteFile : Returns the absolute form of this abstract pathname.
                    // File.separator : The system-dependent default name-separator character. This string contains a single character.

                    val fileOutput = FileOutputStream(file)     // Creates a file output stream to write to the file
                    // represented by the specified object.
                    fileOutput.write(bytes.toByteArray())   // Writes bytes from the specified byte array to this file
                    // output stream.
                    fileOutput.close() // Closes this file output stream and releases any system resources associated with this stream.
                    // This file output stream may no longer be used for writing bytes.

                    result = file.absolutePath  // The file absolute path is return as a result.

                    //Switch from io to ui thread to show a toast
                    runOnUiThread {
                        //Cancel progress dialog in UI thread
                        cancelProgressDialog()
                        if (result.isNotEmpty()) {
                            Toast.makeText(this@MainActivity, "File saved successfully : $result", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something went wrong while saving the file",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }



                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Something went wrong in saving logic : $e", Toast.LENGTH_SHORT)
                        .show()
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)
        /* Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen. */
        customProgressDialog?.setContentView(R.layout.custom_progress_dialog)
        //Start the dialog and display it on screen.
        customProgressDialog?.show()
        
    }
    
    private fun cancelProgressDialog() {
        if (customProgressDialog != null) {
            customProgressDialog?.dismiss()
            customProgressDialog = null
        } 
    }
}

























