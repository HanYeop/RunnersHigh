package com.hanyeop.runnershigh.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.databinding.ActivityMainBinding
import com.hanyeop.runnershigh.util.Constants.Companion.ACTION_SHOW_TRACKING_ACTIVITY
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_FIRST_TIME_TOGGLE
import com.hanyeop.runnershigh.util.Constants.Companion.TAG
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // ActivityMainBinding 선언
    private lateinit var binding : ActivityMainBinding

    // NavController 선언
    private lateinit var navController: NavController

    // SharedPreferences 주입
    @Inject
    lateinit var sharedPref: SharedPreferences

    // 처음 실행 여부
    private var firstTimeAppOpen: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰바인딩
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 데이터 불러오기
        firstTimeAppOpen = sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
        Log.d(TAG, "onCreate: $firstTimeAppOpen")

        // 처음 실행했다면 세팅 화면으로
        if(firstTimeAppOpen){
            val intent = Intent(this,SetupActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.apply {
            // 툴바 추가
            setSupportActionBar(toolbar)

            // 바텀네비게이션뷰 <-> 네비게이션 연결
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.mainContainerView) as NavHostFragment
            navController = navHostFragment.findNavController()
            bottomNavigation.setupWithNavController(navController)
        }
        navigateToTrackingFragmentIfNeeded(intent)
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Snackbar.make(binding.root, "기본 위치 권한이 동의 되었습니다.", Snackbar.LENGTH_SHORT).show()
            }
            else{
                Snackbar.make(binding.root, "권한에 동의하지 않을 경우 이용할 수 없습니다.", Snackbar.LENGTH_SHORT).show()
            }
        }

        else if(requestCode == 2){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Snackbar.make(binding.root, "백그라운드 위치 권한이 동의 되었습니다.", Snackbar.LENGTH_SHORT).show()
            }
            else{
                Snackbar.make(binding.root, "권한에 동의하지 않을 경우 이용할 수 없습니다.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // foreground 상태에서 호출
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    // 알림 창 클릭시 메인 -> Tracking
    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_TRACKING_ACTIVITY) {
            navController.navigate(R.id.trackingActivity)
        }
    }

    // 백버튼 클릭 시 종료
    override fun onBackPressed() {
        finish()
    }
}