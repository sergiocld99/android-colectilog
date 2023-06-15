package cs10.apps.travels.tracer.modules.live.model

import androidx.lifecycle.MutableLiveData

class Countdown {
    private var seconds = 0
    val liveData = MutableLiveData(0)
    private var thread: Thread? = null
    private var run = false

    private fun runBlocking(){
        while (seconds > 0 && run){
            Thread.sleep(1000)
            liveData.postValue(--seconds)
        }
    }

    fun start(startTime: Int){
        seconds = startTime
        run = true

        if (thread == null || thread?.isAlive == false) {
            thread = Thread { runBlocking() }
            thread?.start()
        }
    }

    fun stop(){
        run = false
    }
}