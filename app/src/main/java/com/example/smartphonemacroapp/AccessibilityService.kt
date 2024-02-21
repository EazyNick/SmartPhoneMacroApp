package com.example.smartphonemacroapp

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MyAccessibilityService : AccessibilityService() {
    //private val events = mutableListOf<AccessibilityEvent>()
    //private var recording = false

    private val events = mutableListOf<Map<String, Any?>>()
    private lateinit var prefs: SharedPreferences

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == "isRecording") {
            Log.d("AccessibilityService", "isRecording 찾음")
            val isRecording = sharedPreferences.getBoolean("isRecording", false)
            if (!isRecording) {
                Log.d("AccessibilityService", "json으로 저장")
                // 기록을 중지하고 JSON으로 저장합니다.
                saveEventsToJson()
            }
        }
    }

    private fun saveEventsToJson() {
        Log.d("AccessibilityService", "이벤트 json 저장 함수 실행")
        val gson = Gson()
        val eventsJsonString = gson.toJson(events)

        val fileName = "accessibility_events.json"
        applicationContext.filesDir?.let {
            val file = File(it, fileName)
            FileWriter(file).use { writer ->
                writer.write(eventsJsonString)
            }
        }
    }
//
//    private fun openAccessibilitySettings() {
//        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//    }

//    private fun updateRecordingState(isRecording: Boolean) {
//        val sharedPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
//        with(sharedPrefs.edit()) {
//            putBoolean("isRecording", isRecording)
//            apply()
//        }
//    }

    // AccessibilityService에서 상태 확인
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val sharedPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val isRecording = sharedPrefs.getBoolean("isRecording", false)
            if (isRecording) {
                events.add(
                    mapOf(
                        "eventType" to event.eventType,
                        "contentDescription" to event.contentDescription,
                        "packageName" to event.packageName
                    )
                )
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 서비스 설정을 여기서 구성합니다.
        Log.d("AccessibilityService", "서비스가 연결되었습니다.")
        prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }


    fun playEvents() {
        val json = loadEventsFromJson() // JSON 데이터 로드하는 함수 필요
        val events = parseEvents(json) // JSON 데이터를 파싱하는 함수 필요

        for (event in events) {
            // 각 이벤트에 대한 처리를 수행합니다.
            // 예를 들어, 클릭 이벤트는 특정 위치에 대한 탭으로 재현할 수 있습니다.
            performUserAction(event)
        }
    }

    private fun performUserAction(event: EventData) {
        // 여기에서 event 객체에 따라 실제 사용자 상호작용을 재현합니다.
        // 이는 GestureDescription.Builder를 사용하여 제스처를 만들고
        // dispatchGesture()를 호출하여 실행하는 방법 등이 될 수 있습니다.
    }

    private fun loadEventsFromJson(): String? {
        return try {
            // JSON 파일의 이름을 지정합니다.
            Log.d("AccessibilityService", "Json 파일 로드.")
            val fileName = "accessibility_events.json"

            // 내부 저장소에서 파일을 열어 스트림으로 가져옵니다.
            Log.d("AccessibilityService", "Json 내용 가져오기")
            val file = File(applicationContext.filesDir, fileName)

            // 파일의 모든 내용을 읽어서 문자열로 반환합니다.
            file.readText()
        } catch (e: IOException) {
            // 파일 읽기에 실패한 경우 로그에 오류를 기록하고 null을 반환합니다.
            Log.e("AccessibilityService", "JSON 파일을 로드하는데 실패했습니다.", e)
            null
        }
    }

    override fun onInterrupt() {
    }
}
