package com.example.kidsdrawingapp

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.kidsdrawingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
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
            ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
        )

        binding.ibBrush.setOnClickListener {
            showBrushDialog()
        }

        binding.ibEraser.setOnClickListener {
            showBrushDialog()
            binding.drawingView.setColor("#ffffff")
        }


    }
    
    private fun showBrushDialog () {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn: ImageButton = brushDialog.findViewById(R.id.ibSmallBrush)
        val mediumBtn: ImageButton = brushDialog.findViewById(R.id.ibMediumBrush)
        val largeBtn: ImageButton = brushDialog.findViewById(R.id.ibLargeBrush)
        smallBtn.setOnClickListener{
            binding.drawingView.scaleBrush(10f)
            brushDialog.dismiss()
        }
        mediumBtn.setOnClickListener{
            binding.drawingView.scaleBrush(20f)
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener{
            binding.drawingView.scaleBrush(30f)
            brushDialog.dismiss()
        }
        brushDialog.show()
    }
    
    fun paintClicked (view: View) {
        if (view != globalButtonCurrentPaint) {
            val imageButton  = view as ImageButton
            val colorTag = imageButton.tag.toString()
            binding.drawingView.setColor(colorTag)

            imageButton.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_pressed))

            globalButtonCurrentPaint?.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_normal))

            globalButtonCurrentPaint = view

        } 
    }
    
}






























