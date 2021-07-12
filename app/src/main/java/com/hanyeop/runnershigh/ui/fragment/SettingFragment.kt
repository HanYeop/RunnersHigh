package com.hanyeop.runnershigh.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.databinding.FragmentSettingBinding
import com.hanyeop.runnershigh.util.Constants
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 뷰바인딩
        _binding = FragmentSettingBinding.bind(view)

        loadInformation()

        binding.apply{
            // 모두 삭제 버튼 클릭 시
            allDeleteButton.setOnClickListener {
                viewModel.deleteAllRun()
            }

            modifyButton.setOnClickListener {
//                hideKeyboard()
                val success = changingInformation()
                if(success){
                    Snackbar.make(view,"정보가 수정되었습니다.",Snackbar.LENGTH_SHORT).show()
                }
                else{
                    Snackbar.make(view,"다시 입력해주세요.",Snackbar.LENGTH_SHORT).show()
                }
            }

        }
    }

    // 정보 불러와서 에디트뷰에
    private fun loadInformation(){
        val name = sharedPref.getString(KEY_NAME,"")
        val weight = sharedPref.getFloat(KEY_WEIGHT,70f)
        binding.apply {
            nameText.setText(name)
            weightText.setText(weight.toString())
        }
    }

    // 키, 몸무게 수정 기능
    private fun changingInformation() : Boolean{
        binding.apply {
            val name = nameText.text.toString()
            val weight = weightText.text.toString()

            // 둘중 하나라도 비어 있다면
            if (name.isEmpty() || weight.isEmpty()) {
                return false
            }

            // 키, 몸무게 수정
            sharedPref.edit()
                .putString(Constants.KEY_NAME, name)
                .putFloat(Constants.KEY_WEIGHT, weight.toFloat())
                .apply()
            return true
        }
    }

//    fun hideKeyboard() {
//        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
//    }
}