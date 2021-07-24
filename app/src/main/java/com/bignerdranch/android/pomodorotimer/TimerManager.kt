package com.bignerdranch.android.pomodorotimer

interface TimerManager {

    fun addTimer(timerId: Long, initialTime: Long)
    fun deleteTimer(timerId: Long)

}