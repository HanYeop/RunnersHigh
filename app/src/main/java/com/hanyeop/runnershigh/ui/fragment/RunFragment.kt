package com.hanyeop.runnershigh.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.hanyeop.runnershigh.R
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hanyeop.runnershigh.adapter.RunAdapter
import com.hanyeop.runnershigh.databinding.FragmentRunBinding
import com.hanyeop.runnershigh.util.TrackingUtility
import com.hanyeop.runnershigh.viewmodel.RunViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    // 뷰모델 생성
    private val viewModel by viewModels<RunViewModel>()

    // 참조 관리
    private var _binding : FragmentRunBinding? = null
    private val binding get() = _binding!!

    // RunAdapter 선언
    private lateinit var runAdapter: RunAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰바인딩
        _binding = FragmentRunBinding.bind(view)

        binding.apply {

            // fab 클릭 시 Tracking 화면으로 이등
            runStartFab.setOnClickListener {
                // 권한 체크해서 권한이 있을 때
                if(TrackingUtility.hasLocationPermissions(requireContext())){
                    findNavController().navigate(R.id.action_runFragment_to_trackingActivity)
                }
                // 권한이 없을 때 권한을 요청함
                else {
                    requestPermission()
                }
            }

            // 어댑터 연결
            runAdapter = RunAdapter()
            runRecyclerView.adapter = runAdapter
            runRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })
    }

    // 권한 요청
    private fun requestPermission(){
        // 이미 권한이 있으면 그냥 리턴
        if(TrackingUtility.hasLocationPermissions(requireContext())){
            return
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ), 1)
                permissionDialog(requireContext())
            }
            // API 23 미만 버전에서는 ACCESS_BACKGROUND_LOCATION X
            else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ), 1)
            }
        }
    }

    // 안드로이드 API 30 버전부터는 backgroundPermission 을 직접 설정해야함
    private fun backgroundPermission(){
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ), 2)
    }

    // 백그라운드 권한 요청
    private fun permissionDialog(context : Context){
        var builder = AlertDialog.Builder(context)
        builder.setTitle("백그라운드 위치 권한을 위해 항상 허용으로 설정해주세요.")
            .setPositiveButton("네"){ _,_ ->
                backgroundPermission()
            }
            .setNegativeButton("아니오"){_,_ ->
                null
            }.create()

        builder.show()
    }

    // 프래그먼트는 뷰보다 오래 지속 . 프래그먼트의 onDestroyView() 메서드에서 결합 클래스 인스턴스 참조를 정리
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}