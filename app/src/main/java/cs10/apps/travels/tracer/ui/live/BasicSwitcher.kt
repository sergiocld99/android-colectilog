package cs10.apps.travels.tracer.ui.live

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.TextSwitcher
import cs10.apps.travels.tracer.R

class BasicSwitcher(var textSwitcher: TextSwitcher) {
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var lastAction = LastAction.SLIDED_DOWN
    private var ite = 0

    // content to show
    private val mutableList = mutableListOf<String>()

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
            } else {
                slideDown(mutableList[0])
                ite = 1
            }

            handler?.postDelayed(runnable!!, 4000)
        }

        runnable?.run()
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

    fun getCurrentIteration() : Int = ite

    private fun slideDown(text: String){
        textSwitcher.setInAnimation(getContext(), R.anim.slide_down_in)
        textSwitcher.setOutAnimation(getContext(), R.anim.slide_down_out)
        textSwitcher.setText(text)
        lastAction = LastAction.SLIDED_DOWN
    }

    private fun slideUp(text: String){
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