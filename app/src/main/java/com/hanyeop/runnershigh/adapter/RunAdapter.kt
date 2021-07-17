package com.hanyeop.runnershigh.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hanyeop.runnershigh.databinding.ItemRunBinding
import com.hanyeop.runnershigh.model.Run
import com.hanyeop.runnershigh.ui.dialog.UpdateDialog
import com.hanyeop.runnershigh.ui.dialog.UpdateDialogInterface
import com.hanyeop.runnershigh.util.Constants.Companion.TAG
import com.hanyeop.runnershigh.util.TrackingUtility
import com.hanyeop.runnershigh.viewmodel.RunViewModel
import java.util.*

class RunAdapter(private val runViewModel: RunViewModel) : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(private val binding : ItemRunBinding)
        : RecyclerView.ViewHolder(binding.root), UpdateDialogInterface{
        private lateinit var currentRun : Run

        // 현재 아이템에 맞는 데이터 연결
        fun bind(run : Run){
            binding.apply {
                Glide.with(this.root).load(run.image).into(imageView)

                // 정보 표시
                titleText.text = run.title
                avgSpeedText.text = "${run.avgSpeedInKMH}km/h"
                distanceText.text = "${TrackingUtility.getFormattedDistance(run.distanceInMeters)}km"
                timeText.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
                caloriesText.text = "${run.caloriesBurned}kcal"
                dateText.text = "${run.year}/${run.month}/${run.day}"

                // 아이템 클릭 시 이름 변경 다이얼로그
                itemLayout.setOnClickListener {
                    currentRun = run
                    val myCustomDialog = UpdateDialog(binding.itemLayout.context,this@RunViewHolder,run.title)
                    myCustomDialog.show()
                }
            }
        }

        override fun onOkButtonClicked(content: String) {
            val run = Run(currentRun.id,currentRun.image,currentRun.timestamp,currentRun.avgSpeedInKMH,currentRun.distanceInMeters,
            currentRun.timeInMillis,currentRun.caloriesBurned,content,currentRun.year,currentRun.month,currentRun.day)
            runViewModel.updateRun(run)
            Log.d(TAG, "onOkButtonClicked: $content")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val binding = ItemRunBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RunViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val currentItem = differ.currentList[position]
        // 현재 아이템 넘겨줌
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    // 비동기 처리
    val differ = AsyncListDiffer(this, diffCallback)

    // 데이터 변경
    fun submitList(list: List<Run>) = differ.submitList(list)

    // 효율성을 위해 DiffUtil 사용 (갱신 필요한 것만 갱신)
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Run>() {
            // 아이디가 같은가? => 같으면 areContentsTheSame 으로, 다르면 갱신
            override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
                return oldItem.id == newItem.id
            }

            // 모든 속성이 같은가? => 다르면 갱신
            override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }
}