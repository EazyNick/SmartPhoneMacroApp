package com.example.smartphonemacroapp

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MyAccessibilityService : AccessibilityService() {
    //private val events = mutableListOf<AccessibilityEvent>()
    //private var recording = false

    data class EventData(
        val eventType: String,
        val contentDescription: String?,
        val packageName: String
    )


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

        Log.d("AccessibilityService", "저장할 events 값: $events")

        val eventsJsonString = gson.toJson(events)

        Log.d("AccessibilityService", "eventsJsonString: $eventsJsonString")

        val fileName = "accessibility_events.json"
        applicationContext.filesDir?.let {
            val file = File(it, fileName)
            FileWriter(file).use { writer ->
                writer.write(eventsJsonString)
            }
        }
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // SharedPreferences에서 플래그 확인
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val shouldPlayEvents = prefs.getBoolean("PlayEventsRequested", false)

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                recordEvent(event, "CLICK")
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                recordEvent(event, "FOCUS")
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (event.className == "android.widget.ImageButton" && event.contentDescription == "Navigate up") {
                    recordEvent(event, "BACK")
                }
            }
        }

        if (shouldPlayEvents) {
            // 이벤트 재생 함수 실행
            playEvents()

            // 작업을 수행한 후에는 플래그를 false로 재설정
            prefs.edit().putBoolean("PlayEventsRequested", false).apply()
        }
    }

    private fun recordEvent(event: AccessibilityEvent, action: String) {
        val eventInfo = mapOf(
            "action" to action,
            "packageName" to event.packageName.toString(),
            "timestamp" to System.currentTimeMillis(),
            "contentDescription" to (event.contentDescription?.toString() ?: ""),
            // 여기에 더 많은 정보를 추가할 수 있습니다.
        )
        events.add(eventInfo)
        Log.d("AccessibilityService", "Event recorded: $eventInfo")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 서비스 설정을 여기서 구성합니다.
        Log.d("AccessibilityService", "서비스가 연결되었습니다.")
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }


    fun playEvents() {
        val json = loadEventsFromJson() // JSON 데이터 로드하는 함수 필요
        val events = parseEvents(json) // JSON 데이터를 파싱하는 함수 필요

        Log.d("AccessibilityService", "jSON: $json")
        Log.d("AccessibilityService", "Events: $events")

        for (event in events) {
            Log.d("AccessibilityService", "events 실행: $event")
            // 각 이벤트에 대한 처리를 수행합니다.
            // 예를 들어, 클릭 이벤트는 특정 위치에 대한 탭으로 재현할 수 있습니다.
            performUserAction(event)
        }
    }

    private fun performUserAction(event: EventData) {
        // event 객체에서 필요한 정보를 추출합니다. 예: eventType, packageName 등
        val eventType = event.eventType
        val packageName = event.packageName

        // 루트 노드에서 시작하여 특정 조건을 만족하는 노드를 찾습니다.
        val rootNode = rootInActiveWindow
        val targetNode = rootNode?.findAccessibilityNodeInfosByViewId("viewId")?.firstOrNull()

        // targetNode가 null이 아니라면, 그 노드에 대해 특정 액션을 수행합니다.
        targetNode?.let {
            when (eventType) {
                // 클릭 이벤트를 재현하는 경우
                "typeViewClicked" -> {
                    it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                // 다른 이벤트 유형에 대한 처리를 추가할 수 있습니다.
                else -> {}
            }
        }
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

    private fun parseEvents(jsonData: String?): List<EventData> {
        if (jsonData == null) return emptyList()

        return try {
            Log.d("AccessibilityService", "Json 파싱.")
            val gson = Gson()
            val eventType = object : TypeToken<List<EventData>>() {}.type
            gson.fromJson(jsonData, eventType)
        } catch (e: JsonSyntaxException) {
            Log.e("AccessibilityService", "JSON 파싱 실패", e)
            emptyList()
        }
    }


    override fun onInterrupt() {
    }
}
