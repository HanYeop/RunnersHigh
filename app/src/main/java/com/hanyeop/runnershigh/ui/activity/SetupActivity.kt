package com.hanyeop.runnershigh.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.hanyeop.runnershigh.databinding.ActivitySetupBinding
import com.hanyeop.runnershigh.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupActivity : AppCompatActivity() {

    // ActivitySetupBinding 선언
    private lateinit var binding : ActivitySetupBinding

    // SharedPreferences 주입
    @Inject
    lateinit var sharedPref: SharedPreferences

    // 처음 실행 여부
    @set:Inject
    var firstTimeAppOpen: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(Constants.TAG, "onCreate2: $firstTimeAppOpen")
        // 처음 실행일 때
        if(!firstTimeAppOpen){
            val intent = Intent(this,MainActivity::class.java)
            finish()
            startActivity(intent)
        }

        // 뷰바인딩
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // 완료 버튼 클릭 시
            okButton.setOnClickListener {
                val success = writePersonalDataToSharedPref()
                if (success) {
                    val intent = Intent(this@SetupActivity,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Snackbar.make(
                        binding.root,
                        "정보를 전부 입력해주세요.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private fun writePersonalDataToSharedPref(): Boolean {
        binding.apply {
            val name = nameText.text.toString()
            val weightText = weightText.text.toString()

            // 둘중 하나라도 비어 있다면
            if (name.isEmpty() || weightText.isEmpty()) {
                return false
            }

            // 키, 몸무게 저장하고 처음 실행X 로
            sharedPref.edit()
                .putString(Constants.KEY_NAME, name)
                .putFloat(Constants.KEY_WEIGHT, weightText.toFloat())
                .putBoolean(Constants.KEY_FIRST_TIME_TOGGLE, false)
                .apply()
            return true
        }
    }
}