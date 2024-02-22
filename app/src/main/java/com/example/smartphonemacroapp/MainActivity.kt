package com.example.smartphonemacroapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.smartphonemacroapp.databinding.ActivityMainBinding

// ActivityMainBinding은 뷰 바인딩을 사용하여 XML 레이아웃 파일의 뷰에 대한 직접적인 참조를 제공
// 이를 통해 findViewById 호출 없이 뷰를 조작
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    //private var isRecording = false

    // 액티비티가 생성될 때 호출
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate 시작")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)

        // 앱 시작 시 권한 요청 다이얼로그를 띄웁니다.
        showAccessibilityServiceRequestDialog()
        checkRecordingState()

        binding.startRecordingButton.setOnClickListener {
            // 서비스에 기록 시작을 알립니다.
            // AlertDialog.Builder를 사용하여 대화상자를 생성합니다.
            AlertDialog.Builder(this@MainActivity).apply {
                setTitle("알림") // 대화상자의 제목을 설정합니다.
                setMessage("테스트 대화상자입니다.") // 대화상자의 메시지를 설정합니다.
                setPositiveButton("확인") { dialog, which ->
                    // '확인' 버튼을 누르면 대화상자를 닫습니다.
                    dialog.dismiss()
                }
                // 대화상자를 화면에 표시합니다.
                show()
            }
            // SharedPreferences를 사용하여 기록 상태를 변경하고 저장합니다.
            val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putBoolean("isRecording", true) // 기록을 시작하는 상태로 변경
                apply()
            }

            // 상태 변경 후 필요한 UI 업데이트 또는 작업을 수행
            checkRecordingState() // UI를 업데이트하기 위해 상태를 확인
            //service.startRecording()
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        }

        binding.stopRecordingButton.setOnClickListener {
            // SharedPreferences를 사용하여 기록 상태를 변경하고 저장합니다.
            val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putBoolean("isRecording", false)
                apply()
            }
            
            Log.d("MainActivity", "기록 상태를 false로 변경")
            // "기록 시작" 버튼의 텍스트를 업데이트합니다.
            binding.startRecordingButton.text = "기록 시작"
        }

        binding.playEventsButton.setOnClickListener {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)

            // SharedPreferences에 이벤트 재생을 위한 플래그 설정
            val prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("PlayEventsRequested", true).apply()
        }

    }

    // 사용자를 안드로이드의 접근성 설정 화면으로 이동시키는 인텐트를 실행하는 함수
    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    // 사용자에게 접근성 서비스 활성화를 요청하는 대화상자를 표시하는 함수
    fun showAccessibilityServiceRequestDialog() {
        val prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isServiceRequestedBefore = prefs.getBoolean("isServiceRequested", false)
        val isServiceEnabled = isAccessibilityServiceEnabled()

        Log.d("MainActivity", "조건1: $isServiceRequestedBefore")
        Log.d("MainActivity", "조건2: $isServiceEnabled")

        //if (!isServiceRequestedBefore && !isServiceEnabled) {
        if (!isServiceRequestedBefore or  !isServiceEnabled) {

            Log.d("MainActivity", "권한 요청")
            AlertDialog.Builder(this)
                .setTitle("권한 요청")
                .setMessage("이 앱을 사용하기 위해서는 접근성 서비스를 활성화해야 합니다. 활성화 하시겠습니까?")
                .setPositiveButton("예") { dialog, which ->
                    prefs.edit().putBoolean("isServiceRequested", true).apply()
                    openAccessibilitySettings()
                }
                .setNegativeButton("아니오", null)
                .show()
        }
        else {
            Log.d("MainActivity", "이미 권한 요청 완료")
        }
    }

    // 현재 기록 상태(isRecording)를 확인하고, 이에 따라 "기록 시작"/"기록 중" 버튼의 텍스트를 업데이트하는 함수
    fun checkRecordingState() {
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isRecording = sharedPrefs.getBoolean("isRecording", false)

        val startRecordingButton = findViewById<Button>(R.id.startRecordingButton)
        // 기록 상태에 따라 버튼의 텍스트를 업데이트합니다.
        Log.d("MainActivity", "스타트버튼 기록 중 or 기록 시작으로 변경")
        startRecordingButton.text = if (isRecording) "기록 중" else "기록 시작"
    }

        fun isAccessibilityServiceEnabled(): Boolean {
            Log.d("MainActivity", "접근성 활성화 확인중")
            val service = getString(R.string.accessibility_service_id).replace("/", ".") // "/"를 "."로 변경하는 대신 올바른 형식 사용
            val serviceId = service.replace("..", ".")
            val enabledServicesSetting = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            // strings.xml 파일에서 accessibility_service_id에 해당하는 리소스 값을 가져옵니다.
            // 이 값은 접근성 서비스의 전체 경로(패키지 이름과 클래스 이름)를 나타냅니다.

            Log.d("MainActivity", "Service ID for check: $serviceId")
            Log.d("MainActivity", "Enabled Services: $enabledServicesSetting")

    //        val service = getString(R.string.accessibility_service_id)
    //        Log.d("MainActivity", "Service ID: $service")

            // 반환된 활성화된 접근성 서비스 목록 문자열에서 해당 서비스의 전체 경로를 포함하고 있는지 확인합니다.
            // 포함하고 있다면, 해당 서비스가 활성화되어 있다는 의미이므로 true를 반환합니다.
            // accessibilityServices가 null일 경우, ?: 연산자를 사용하여 false를 반환합니다.
            // contains 메서드를 사용하여 서비스가 활성화된 접근성 서비스 목록에 포함되어 있는지 검사합니다.
            val isServiceEnabled = enabledServicesSetting?.contains(serviceId) ?: false

            Log.d("MainActivity", "접근성 서비스 활성화 상태: $isServiceEnabled")
            return isServiceEnabled
        }

    // 액티비티가 사용자와 상호작용하기 시작할 때 호출
    // 접근성 서비스 활성화 여부를 확인하고, 필요한 경우 사용자에게 접근성 서비스 활성화를 요청하는 대화상자를 표시
    override fun onResume() {
        super.onResume()
        if (isAccessibilityServiceEnabled()) {
            Log.d("MainActivity", "접근성 설정 확인완료")
            // 서비스가 활성화되었을 경우의 로직
        } else {
            // 서비스가 활성화되지 않았을 경우, 설정 화면으로 유도
            Log.d("MainActivity", "접근성 설정 확인되지 않아 다시 설정필요")
            showAccessibilityServiceRequestDialog()
        }
    }


    // 서비스 바인딩 로직을 추가해야 합니다.
}