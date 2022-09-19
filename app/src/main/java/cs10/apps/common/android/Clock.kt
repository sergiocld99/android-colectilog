package cs10.apps.common.android

class Clock(private val runnable: Runnable, private val sleepTime: Long) {

    private var thread : Thread? = null
    private var active = false

    fun start(){
        if (thread != null) return

        thread = Thread {
            active = true

            while (active){
                runnable.run()
                Thread.sleep(sleepTime)
            }
        }

        thread!!.start()
    }

    fun restart(){
        thread?.let {
            it.interrupt()
            stop()
        }

        // start when thread is destroyed
        start()
    }

    fun stop(){
        active = false
        thread = null
    }
}