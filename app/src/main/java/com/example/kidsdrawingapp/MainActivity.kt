package com.example.kidsdrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.example.kidsdrawingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Scale brush with default size of 20
        binding.drawingView.scaleBrush(20f)

        binding.ibBrush.setOnClickListener {
            showBrushDialog()
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
}






























