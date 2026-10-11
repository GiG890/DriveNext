package com.example.drivenext.presentation.noconnection

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.drivenext.R
import com.example.drivenext.data.repository.NetworkRepositoryImpl
import com.example.drivenext.presentation.splash.SplashActivity

class NoConnectionActivity : AppCompatActivity() {

    private val viewModel: NoConnectionViewModel by viewModels {
        NoConnectionViewModelFactory(
            networkRepository = NetworkRepositoryImpl(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_connection)

        val btnRetry = findViewById<Button>(R.id.btnRetry)
        btnRetry.setOnClickListener {
            viewModel.onRetryClicked()
        }

        viewModel.networkRestored.observe(this) { restored ->
            if (restored) {
                startActivity(Intent(this, SplashActivity::class.java))
                finish()
            }
        }

        viewModel.showErrorMessage.observe(this) { show ->
            if (show) {
                Toast.makeText(this, "Интернет не появился. Попробуйте ещё раз.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}