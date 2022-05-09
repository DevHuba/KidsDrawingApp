package com.example.kidsdrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
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
import com.example.kidsdrawingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val galleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            binding.ivCustomImage.setImageURI(result.data?.data)
        }
    }

    //Multiple permission
    private val storageResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    Toast.makeText(this, "Permission $permissionName Granted", Toast.LENGTH_SHORT).show()
                    //Use special intent for media store
                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    //pick image from gallery
                    galleryLauncher.launch(pickIntent)


                } else {
                    Toast.makeText(this, "Permission $permissionName Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }


    //Variables
    private lateinit var binding: ActivityMainBinding
    private var globalButtonCurrentPaint: ImageButton? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Scale brush with default size of 20
        binding.drawingView.scaleBrush(20f)

        val llPaintColors = binding.llPaintColors
        globalButtonCurrentPaint = llPaintColors[1] as ImageButton
        globalButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )
        binding.ibImage.setOnClickListener {
            requestStoragePermission()
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

    }

    //Show rationale dialog for displaying why we need permission
    private fun showRationaleDialog(title:String, message:String){
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
    
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        ){
            showRationaleDialog("Kids Drawing App","Kids Drawing App needs to Access Your External Storage")
        }
        else {
            storageResultLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }


}

























