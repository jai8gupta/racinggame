package com.example.racinggame

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.racinggame.databinding.ActivityStartUpScreenBinding

class StartUpScreen : AppCompatActivity() {
    private lateinit var binding: ActivityStartUpScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartUpScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val startBtn = binding.startButton;
        val exitBtn = binding.exitButton;
        val navigationIntent = Intent(this, MainActivity2::class.java);
        startBtn.setOnClickListener {
            startActivity(navigationIntent);
        }
        exitBtn.setOnClickListener {
            finishAffinity();
        }

    }


}