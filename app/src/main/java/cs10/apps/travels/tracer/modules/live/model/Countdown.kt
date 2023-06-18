package cs10.apps.travels.tracer.modules.live.model

import androidx.lifecycle.MutableLiveData
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Countdown {
    private var seconds = 0
    val liveData = MutableLiveData(0)
    private var thread: Thread? = null
    private var run = false
    private var interval = 1000L

    private fun runBlocking(){
        while (seconds > 0 && run){
            Thread.sleep(interval)
            liveData.postValue(--seconds)
        }
    }

    /**
     * @return true if internal time was replaced
     */
    fun start(startTime: Int) : Boolean {
        run = true

        if (thread == null || thread?.isAlive == false) {
            thread = Thread { runBlocking() }
            interval = 1000
            seconds = startTime
            thread?.start()
        } else {
            when {
                abs(seconds - startTime) > 120 -> {
                    seconds = startTime
                    return true
                }
                this.seconds > startTime -> acelerar()
                else -> frenar()
            }
        }

        return false
    }

    private fun acelerar(){
        interval = max(interval * 0.8, 600.0).toLong()
    }

    private fun frenar(){
        interval = min(interval * 1.2, 1440.0).toLong()
    }

    fun stop(){
        run = false
    }
}