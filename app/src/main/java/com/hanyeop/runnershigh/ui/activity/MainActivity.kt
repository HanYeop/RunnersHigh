package com.hanyeop.runnershigh.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // ActivityMainBinding 선언
    private lateinit var binding : ActivityMainBinding

    // NavController 선언
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰바인딩
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            // 툴바 추가
            setSupportActionBar(toolbar)

            // 바텀네비게이션뷰 <-> 네비게이션 연결
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.mainContainerView) as NavHostFragment
            navController = navHostFragment.findNavController()
            bottomNavigation.setupWithNavController(navController)
        }
    }
}