package com.bignerdranch.android.pomodorotimer

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.util.Log
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.pomodorotimer.databinding.PomodorotimerItemBinding


class PomodoroTimerViewHolder(
    private val binding: PomodorotimerItemBinding,
    private val resources: Resources,
    private val adapter: PomodoroTimerAdapter,
) : RecyclerView.ViewHolder(binding.root) {

    private val TAG = "PomodoroTimerViewHolder"
    private var timerItem: TimerItem? = null
    private val viewHolderIndex = ++index

    private val stateObserver = object : Observer<TimerItem.State> {

        override fun onChanged(state: TimerItem.State?) {
            Log.d(
                TAG,
                "ViewHolder #$viewHolderIndex state update for $timerItem. New state: $state"
            )
            if (timerItem == null) return

            when (state) {
                TimerItem.State.CREATED -> {
                    binding.constraintLayoutItem.setBackgroundColor(resources.getColor(R.color.white))
                    binding.startStopButton.isVisible = true
                    binding.startStopButton.text = resources.getString(R.string.start_button)
                    binding.startStopButton.isEnabled = true
                    binding.blinkingIndicator.isInvisible = true
                    (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
                    binding.progressBar.isVisible = true
                    binding.deleteButton.isEnabled = true
                }
                TimerItem.State.RUNNING -> {
                    binding.startStopButton.isVisible = true
                    binding.startStopButton.text = resources.getString(R.string.stop_button)
                    //мигающий иникатор
                    binding.blinkingIndicator.isInvisible = false
                    (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
                    binding.deleteButton.isEnabled = true
                }
                TimerItem.State.PAUSED -> {
                    binding.startStopButton.isVisible = true
                    binding.startStopButton.text = resources.getString(R.string.start_button)
                    binding.blinkingIndicator.isInvisible = true
                    (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
                    binding.deleteButton.isEnabled = true

                    binding.constraintLayoutItem.setBackgroundColor(resources.getColor(R.color.white))
                    binding.startStopButton.isEnabled = true
                    binding.progressBar.isVisible = true
                }
                TimerItem.State.FINISHED -> timerFinish()
            }
        }
    }

    private val timeObserver = object : Observer<Long> {
        override fun onChanged(currentTimeMs: Long) {
            if (timerItem == null) return
            binding.stopwatchTimer.text = currentTimeMs.displayTime()
            binding.progressBar.setPeriod(timerItem!!.initialTimeMs)
            binding.progressBar.setCurrent(currentTimeMs)
        }
    }

    fun bind(item: TimerItem) {

        // cleanup
        this.timerItem?.stateLiveData?.removeObserver(stateObserver)
        this.timerItem?.currentTimeMsLiveData?.removeObserver(timeObserver)
        this.timerItem = item
        Log.d(TAG, "ViewHolder #$viewHolderIndex bind witth ${this.timerItem}")

        // set values
        this.timerItem!!.stateLiveData.observe(
            this.itemView.context as LifecycleOwner,
            stateObserver
        )
        this.timerItem!!.currentTimeMsLiveData.observe(
            this.itemView.context as LifecycleOwner,
            timeObserver
        )

        binding.startStopButton.setOnClickListener {
            if (this.timerItem!!.stateLiveData.value == TimerItem.State.CREATED ||
                this.timerItem!!.stateLiveData.value == TimerItem.State.PAUSED
            ) {
                Log.d(TAG, "ViewHolder #$viewHolderIndex starting ${this.timerItem}")
                adapter.stopOther(this.timerItem!!.id)
                this.timerItem!!.start()

            } else if (this.timerItem!!.stateLiveData.value == TimerItem.State.RUNNING) {
                Log.d(TAG, "ViewHolder #$viewHolderIndex stopping ${this.timerItem}")
                this.timerItem!!.stop()
            }
        }

        binding.deleteButton.setOnClickListener {
            adapter.deleteTimer(this.timerItem!!.id)
            binding.deleteButton.isEnabled = false
        }
    }

    private fun timerFinish() {
        binding.stopwatchTimer.text = START_TIME
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        binding.blinkingIndicator.isInvisible = true
        binding.startStopButton.isEnabled = false
        binding.startStopButton.isVisible = false
        binding.constraintLayoutItem.setBackgroundColor(resources.getColor(R.color.deep_red_dark_100))
        binding.progressBar.isVisible = false
        binding.deleteButton.isEnabled = true
    }

    companion object {
        private var index = 0
    }
}


