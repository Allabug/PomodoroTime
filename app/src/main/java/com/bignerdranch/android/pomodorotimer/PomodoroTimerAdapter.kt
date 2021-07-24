package com.bignerdranch.android.pomodorotimer


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bignerdranch.android.pomodorotimer.databinding.PomodorotimerItemBinding

class PomodoroTimerAdapter(
    private val timerManager: TimerManager,
) : ListAdapter<TimerItem, PomodoroTimerViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PomodoroTimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PomodorotimerItemBinding.inflate(layoutInflater, parent, false)
        return PomodoroTimerViewHolder(binding, binding.root.context.resources, this)
    }

    override fun onBindViewHolder(holder: PomodoroTimerViewHolder, position: Int) {
        holder.bind(getItem(position)) // для конкретного ViewHolder обновляем параметры
    }

    fun stopOther(currentId: Long) {
        val currentList = currentList
        currentList.forEach {
            if (it.id != currentId) {
                it.stop()
            }
        }
    }

    fun deleteTimer(id: Long) {
        timerManager.deleteTimer(id)
    }

    private companion object {

        private val itemComparator = object : DiffUtil.ItemCallback<TimerItem>() {
            override fun areItemsTheSame(oldItem: TimerItem, newItem: TimerItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TimerItem, newItem: TimerItem): Boolean {
                return oldItem.currentTimeMsLiveData.value == newItem.currentTimeMsLiveData.value
            }

            override fun getChangePayload(oldItem: TimerItem, newItem: TimerItem) = Any()
        }
    }
}

