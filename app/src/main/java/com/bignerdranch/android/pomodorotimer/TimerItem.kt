package com.bignerdranch.android.pomodorotimer

import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Возможные состояния
 * 1. создался
 * 2. таймер идет
 * 3. закончился
 * 4. пауза
 */
data class TimerItem(
    val id: Long,
    var initialTimeMs: Long, // время которое ввел пользователь
    private val hiddenId: Int = ++timerId,
) {

    enum class State {
        CREATED,//создан
        RUNNING,//запущен
        FINISHED,//отработал
        PAUSED,//приостановлен
    }

    private val TAG = "TimerItem"
    private var timer: CountDownTimer? = null

    private val _stateLiveData = MutableLiveData(State.CREATED)
    val stateLiveData: LiveData<State> = _stateLiveData

//    var mediaPlayer = MediaPlayer.create(mainActivity, R.raw.pristine)

    //актуальное время на данный момент
    private val _currentTimeMsLiveData = MutableLiveData<Long>(initialTimeMs)
    val currentTimeMsLiveData: LiveData<Long> = _currentTimeMsLiveData

    //время когда пользователь нажал кнопку START, берем из системы
    private var startTimeMs: Long = 0L

    init {
        Log.d(TAG, "Initialized with HIDDEN_ID $hiddenId")
    }

    private fun setState(state: State) {
        _stateLiveData.value = state
    }

    fun start() {
        Log.d(TAG, "#$hiddenId start")
        startTimeMs = System.currentTimeMillis()
        timer = object : CountDownTimer(_currentTimeMsLiveData.value!!, PERIOD) {

            override fun onTick(millisUntilFinished: Long) {
                Log.d(
                    TAG,
                    "Timer #$id current time millis left: $millisUntilFinished and Initial times: $initialTimeMs"
                )
                _currentTimeMsLiveData.value = millisUntilFinished
            }

            override fun onFinish() {
                finish()
                Log.d(TAG, "Timer #$id is finished")
//                mediaPlayer.start()
            }

        }
        timer!!.start()
        setState(State.RUNNING)
    }

    fun stop() {
        Log.d(TAG, "#$hiddenId stopped")
        if (_stateLiveData.value == State.RUNNING) {
            timer?.cancel()
            setState(State.PAUSED)
        }
    }

    fun finish() {
        Log.d(TAG, "#$hiddenId finished")
        setState(State.FINISHED)
    }

    companion object {
        private const val PERIOD = 1000L
        private var timerId = 0
    }
}
