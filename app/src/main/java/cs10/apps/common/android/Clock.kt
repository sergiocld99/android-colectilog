package cs10.apps.common.android

import cs10.apps.travels.tracer.viewmodel.ServiceVM
import java.util.*

class Clock(private val serviceVM: ServiceVM) {

    private var thread : Thread? = null
    private var active = false

    private fun reload(){
        val calendar = Calendar.getInstance()
        val h = calendar.get(Calendar.HOUR_OF_DAY)
        val m = calendar.get(Calendar.MINUTE)

        serviceVM.setCurrentTime(h,m)
    }

    fun start(){
        if (thread != null) return

        thread = Thread {
            active = true

            while (active){
                reload()
                Thread.sleep(10000)
            }
        }

        thread!!.start()
    }

    fun stop(){
        active = false
        thread = null
    }
}