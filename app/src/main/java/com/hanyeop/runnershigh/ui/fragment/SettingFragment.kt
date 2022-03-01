package com.hanyeop.runnershigh.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.databinding.FragmentSettingBinding
import com.hanyeop.runnershigh.util.Constants
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_COLOR
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_NAME
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_WEIGHT
import com.hanyeop.runnershigh.viewmodel.RunViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : Fragment(R.layout.fragment_setting) {

    // 뷰모델 생성
    private val viewModel by viewModels<RunViewModel>()

    // 참조 관리
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    // SharedPreferences 주입
    @Inject
    lateinit var sharedPref: SharedPreferences

    // 현재 색상
    private var colorState : Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 뷰바인딩
        _binding = FragmentSettingBinding.bind(view)

        loadInformation()

        binding.apply{
            // 모두 삭제 버튼 클릭 시
            allDeleteButton.setOnClickListener {
                var builder = AlertDialog.Builder(context)
                builder.setTitle("정말 모든 기록을 삭제할까요? 삭제한 기록은 복구되지 않습니다.")
                    .setPositiveButton("네"){ _,_ ->
                        viewModel.deleteAllRun()
                        Snackbar.make(view,"기록이 전부 삭제 되었습니다.",Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("아니오"){_,_ ->
                        null
                    }.create()

                builder.show()
            }

            // 변경 버튼 클릭 시
            profileEditButton.setOnClickListener {
                hideKeyBoard()
                val success = changingInformation()
                if(success){
                    Snackbar.make(view,"프로필 정보가 수정되었습니다.",Snackbar.LENGTH_SHORT).show()
                }
                else{
                    Snackbar.make(view,"다시 입력해주세요.",Snackbar.LENGTH_SHORT).show()
                }
            }

            // 색상 원 선택 시
            redCircle.setOnClickListener {
                colorState = 1
                selectColorCircle()
            }
            blueCircle.setOnClickListener {
                colorState = 2
                selectColorCircle()
            }
            greenCircle.setOnClickListener {
                colorState = 3
                selectColorCircle()
            }
            blackCircle.setOnClickListener {
                colorState = 4
                selectColorCircle()
            }

            // 선 색 변경 버튼 클릭 시
            colorEditButton.setOnClickListener {
                val success = changingColor()
                if(success){
                    Snackbar.make(view,"선 색상이 변경되었습니다.",Snackbar.LENGTH_SHORT).show()
                }
                else{
                    Snackbar.make(view,"다시 선택해주세요.",Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 정보 불러오기
    private fun loadInformation(){
        // 프로필 정보
        val name = sharedPref.getString(KEY_NAME,"")
        val weight = sharedPref.getFloat(KEY_WEIGHT,70f)
        binding.apply {
            nameText.setText(name)
            weightText.setText(weight.toString())
        }

        // 선 색상 정보
        colorState = sharedPref.getInt(KEY_COLOR,1)
        selectColorCircle()
    }

    // 이름, 몸무게 수정 기능
    private fun changingInformation() : Boolean{
        binding.apply {
            val name = nameText.text.toString()
            val weight = weightText.text.toString()

            // 둘중 하나라도 비어 있다면
            if (name.isEmpty() || weight.isEmpty()) {
                return false
            }

            // 이름, 몸무게 수정
            sharedPref.edit()
                .putString(Constants.KEY_NAME, name)
                .putFloat(Constants.KEY_WEIGHT, weight.toFloat())
                .apply()

            // 툴바 텍스트 랜덤 변경
            val toolbarTitle : TextView= requireActivity().findViewById(R.id.toolbarTitle)
            toolbarTitle.text = "${name}님, 안녕하세요!"
            return true
        }
    }

    // 색상 원 선택 체크
    private fun selectColorCircle(){
        binding.apply {
            when (colorState) {
                1 -> {
                    redCheck.visibility = View.VISIBLE
                    blueCheck.visibility = View.GONE
                    greenCheck.visibility = View.GONE
                    blackCheck.visibility = View.GONE
                }
                2 -> {
                    redCheck.visibility = View.GONE
                    blueCheck.visibility = View.VISIBLE
                    greenCheck.visibility = View.GONE
                    blackCheck.visibility = View.GONE
                }
                3 -> {
                    redCheck.visibility = View.GONE
                    blueCheck.visibility = View.GONE
                    greenCheck.visibility = View.VISIBLE
                    blackCheck.visibility = View.GONE
                }
                4 -> {
                    redCheck.visibility = View.GONE
                    blueCheck.visibility = View.GONE
                    greenCheck.visibility = View.GONE
                    blackCheck.visibility = View.VISIBLE
                }
            }
        }
    }

    // 선 색상 변경
    private fun changingColor() : Boolean {
        binding.apply {
            // 선 색상 정보 저장
            sharedPref.edit()
                .putInt(Constants.KEY_COLOR, colorState)
                .apply()
            return true
        }
        return false;
    }

    // 키보드 내리기
    private fun hideKeyBoard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    // 프래그먼트는 뷰보다 오래 지속 . 프래그먼트의 onDestroyView() 메서드에서 결합 클래스 인스턴스 참조를 정리
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}