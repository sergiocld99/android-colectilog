package cs10.apps.travels.tracer.modules.live.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.TextSwitcher
import cs10.apps.travels.tracer.R

class BasicSwitcher(private var textSwitcher: TextSwitcher, private val autoRepeat: Boolean = true) {
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var lastAction = LastAction.SLIDED_DOWN
    private var ite = 0

    // content to show
    private val mutableList = mutableListOf<String>()
    private var lastTextShown: String? = null

    fun addContent(text: String) {
        mutableList.add(text)
    }

    fun replaceContent(text: String, index: Int){
        if (index >= mutableList.size) mutableList.add(text)
        else mutableList[index] = text
    }

    fun start(){
        if (handler != null || mutableList.isEmpty()) return

        handler = Handler(Looper.getMainLooper())
        ite = 0

        runnable = Runnable {
            if (ite < mutableList.size){
                slideUp(mutableList[ite])
                ite++
                scheduleNext(16000L / mutableList.size)
            } else {
                slideDown(mutableList[0])
                ite = 1
                if (autoRepeat) scheduleNext(20000L / mutableList.size)
            }
        }

        runnable?.run()
    }

    private fun scheduleNext(ms: Long){
        handler?.postDelayed(runnable!!, ms)
    }

    fun stop(){
        handler?.let {
            it.removeCallbacks(runnable!!)
            handler = null
        }
    }

    fun clear(){
        stop()
        mutableList.clear()
        ite = 0
    }

    fun purge(){
        clear()
        slideDown(null)
    }

    fun getCurrentIteration() : Int = ite

    private fun slideDown(text: String?){
        if (lastTextShown == text) return
        else lastTextShown = text

        textSwitcher.setInAnimation(getContext(), R.anim.slide_down_in)
        textSwitcher.setOutAnimation(getContext(), R.anim.slide_down_out)
        textSwitcher.setText(text)
        lastAction = LastAction.SLIDED_DOWN
    }

    private fun slideUp(text: String?){
        if (lastTextShown == text) return
        else lastTextShown = text

        textSwitcher.setInAnimation(getContext(), R.anim.slide_up_in)
        textSwitcher.setOutAnimation(getContext(), R.anim.slide_up_out)
        textSwitcher.setText(text)
        lastAction = LastAction.SLIDED_UP
    }

    fun getContext() : Context {
        return textSwitcher.context
    }

    private enum class LastAction {
        SLIDED_DOWN, SLIDED_UP
    }
}