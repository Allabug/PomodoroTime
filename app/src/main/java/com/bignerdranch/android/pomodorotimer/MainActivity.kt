package com.bignerdranch.android.pomodorotimer


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.pomodorotimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TimerManager, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"
    private val pomodoroTimerAdapter = PomodoroTimerAdapter(this)
    private var nextId = 0L
    private var minutes: Long? = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        //add binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pomodoroTimerAdapter

        }
        if (!TimerContainer.timers.isNullOrEmpty()) {
            pomodoroTimerAdapter.submitList(TimerContainer.timers)
        }


        binding.editTextMinute.afterTextChanged { minutes ->
            Log.d(TAG, "Minutes = $minutes")
            if (minutes.isNotBlank() && minutes.isDigitsOnly()) {
                this.minutes = minutes.toLong() * 1000L * 60L
                Log.d(TAG, "This minutes = ${this.minutes} ")
            }
            if (minutes.isBlank()) {
                this.minutes = 0L
            }
        }


        binding.addNewTimerButton.setOnClickListener {
            if (this.minutes != 0L && this.minutes!! <= ONE_HOUR) {
                addTimer(++nextId, minutes!!)
            }else{
                binding.editTextMinute.error = "Enter minutes from 1 to 60"
            }
        }
    }

    override fun addTimer(timerId: Long, initialTime: Long) {
        TimerContainer.timers.add(
            TimerItem(
                id = timerId,
                initialTimeMs = initialTime,
                
            )
        )
        Log.d(TAG, "Timer with id: $timerId has been added")
        pomodoroTimerAdapter.submitList(TimerContainer.timers.toList())//передаем в адаптер лист с таймерами
    }

    override fun deleteTimer(timerId: Long) {
        val indexForDelete = TimerContainer.timers.indexOfFirst { it.id == timerId }
        val removedTimer = TimerContainer.timers.removeAt(indexForDelete)
        removedTimer.stop() // just make sure that it's stopped.
        Log.d(TAG, "Timer with id: ${removedTimer.id} successfully removed")
        pomodoroTimerAdapter.submitList(TimerContainer.timers.toList())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (!TimerContainer.timers.isNullOrEmpty()) {
            val indexRunningTimer = getTimerIndex()
            Log.d(TAG, "Timer index: $indexRunningTimer ")
            if (indexRunningTimer >= 0) {
                val currentMs = getMillisRunningTimer(indexRunningTimer)
                Log.d(TAG, "Timer current millis: $currentMs successfully removed")
                val startIntent = Intent(this, ForegroundService::class.java)
                startIntent.putExtra(COMMAND_ID, COMMAND_START)
                startIntent.putExtra(STARTED_TIMER_TIME_MS, currentMs)
                startService(startIntent)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppStop() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    private fun getTimerIndex(): Int {
        return TimerContainer.timers.indexOfFirst {
            it.stateLiveData.value == TimerItem.State.RUNNING
        }
    }

    private fun getMillisRunningTimer(index: Int): Long? {
        return TimerContainer.timers[index].currentTimeMsLiveData.value
    }

    companion object {
        const val ONE_HOUR = 60 * 60 * 1000L
    }

}
