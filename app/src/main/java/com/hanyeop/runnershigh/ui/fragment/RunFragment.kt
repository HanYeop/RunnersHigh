package com.hanyeop.runnershigh.ui.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hanyeop.runnershigh.R
import com.hanyeop.runnershigh.adapter.RunAdapter
import com.hanyeop.runnershigh.databinding.FragmentRunBinding
import com.hanyeop.runnershigh.util.SortType
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

            // 스피너 값 변경시 뷰모델에 전달
            sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(adapterView: AdapterView<*>?) {}

                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    when (pos) {
                        0 -> viewModel.sortRuns(SortType.DATE)
                        1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                        2 -> viewModel.sortRuns(SortType.DISTANCE)
                        3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                        4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                    }
                }
            }

            // 어댑터 연결
            runAdapter = RunAdapter(viewModel)
            runRecyclerView.adapter = runAdapter
            runRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            // 삭제 리스너 연결
            ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(runRecyclerView)

        }

        // 목록 관찰하여 변경시 목록 변경
        viewModel.runs.observe(viewLifecycleOwner, Observer {
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

    // 기록 스와이프하여 삭제
    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val run = runAdapter.differ.currentList[position]
            viewModel.deleteRun(run)
            Snackbar.make(requireView(), "기록을 삭제했습니다.", Snackbar.LENGTH_LONG).apply {
                setAction("되돌리기") {
                    viewModel.insertRun(run)
                }
                show()
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