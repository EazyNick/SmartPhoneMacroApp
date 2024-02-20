package com.example.smartphonemacroapp

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.smartphonemacroapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            startRecordButton.setOnClickListener {
                if (!isRecording) {
                    startRecordButton.text = "기록중단 및 저장"
                    isRecording = true
                    // 기록 시작 관련 코드를 여기에 추가
                } else {
                    startRecordButton.text = "기록시작"
                    isRecording = false
                    // 기록 중단 및 저장 관련 코드를 여기에 추가
                }
            }

            loadButton.setOnClickListener {
                // 불러오기 관련 코드를 여기에 추가
            }

            executeButton.setOnClickListener {
                // 실행하기 관련 코드를 여기에 추가
            }
        }
    }
}