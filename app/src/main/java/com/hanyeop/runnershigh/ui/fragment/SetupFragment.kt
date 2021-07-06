package com.hanyeop.runnershigh.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.databinding.FragmentSetupBinding
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_FIRST_TIME_TOGGLE
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_NAME
import com.hanyeop.runnershigh.util.Constants.Companion.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    // 참조 관리
    private var _binding : FragmentSetupBinding? = null
    private val binding get() = _binding!!

    // SharedPreferences 주입
    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var firstTimeAppOpen: Boolean = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰바인딩
        _binding = FragmentSetupBinding.bind(view)

        binding.apply {
//            if (!firstTimeAppOpen) {
//                val navOptions = NavOptions.Builder()
//                    .setPopUpTo(R.id.setupFragment, true)
//                    .build()
//                findNavController().navigate(
//                    R.id.action_setupFragment_to_runFragment,
//                    savedInstanceState,
//                    navOptions
//                )
//            }

//            okButton.setOnClickListener {
//                val success = writePersonalDataToSharedPref()
//                if (success) {
//                    findNavController().navigate(R.id.action_setupFragment_to_runFragment)
//                } else {
//                    Snackbar.make(
//                        requireView(),
//                        "정보를 전부 입력해주세요.",
//                        Snackbar.LENGTH_SHORT
//                    )
//                        .show()
//                }
//            }
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
                .putString(KEY_NAME, name)
                .putFloat(KEY_WEIGHT, weightText.toFloat())
                .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
                .apply()
            return true
        }
    }

    // 프래그먼트는 뷰보다 오래 지속 . 프래그먼트의 onDestroyView() 메서드에서 결합 클래스 인스턴스 참조를 정리
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}