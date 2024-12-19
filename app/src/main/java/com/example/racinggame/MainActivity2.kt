package com.example.racinggame

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.racinggame.databinding.ActivityMainBinding

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding;
    private lateinit var scoreText: TextView;
    private lateinit var gameView: GameView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        scoreText = binding.scoreText;
        gameView = binding.gameView;

        gameView.setOnUpdateScoreListener { score ->
            runOnUiThread {
                Log.d("Score is", score.toString())
                scoreText.text = "Score $score"
            }
        }
        gameView.setOnGameOverListener {
            runOnUiThread{
                showGameOverDialog()
            }
        }
    }
    private fun showGameOverDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Your score: ${scoreText.text}")
            .setPositiveButton("Restart") { _, _ ->
                gameView.restartGame()
            }
            .setNegativeButton("Quit") { _, _ ->
                finish()
                gameView.releaseSounds();
            }
            .create()
        dialog.show()
    }
}