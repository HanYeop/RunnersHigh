package com.hanyeop.runnershigh.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hanyeop.runnershigh.databinding.ItemRunBinding
import com.hanyeop.runnershigh.model.Run
import com.hanyeop.runnershigh.util.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(private val binding : ItemRunBinding) : RecyclerView.ViewHolder(binding.root){
        // 현재 아이템에 맞는 데이터 연결
        fun bind(run : Run){
            binding.apply {
                Glide.with(this.root).load(run.image).into(imageView)

                /**
                 * 날짜 변환
                 */
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = run.timestamp
                }
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
                val dayFormat = SimpleDateFormat("dd", Locale.getDefault())

                titleText.text = "${yearFormat.format(calendar.time)}년 ${monthFormat.format(calendar.time)}월 " +
                        "${dayFormat.format(calendar.time)}일 러닝"
                avgSpeedText.text = "${run.avgSpeedInKMH}km/h"
                distanceText.text = "${run.distanceInMeters / 1000f}km"
                timeText.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
                caloriesText.text = "${run.caloriesBurned}kcal"
            }
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