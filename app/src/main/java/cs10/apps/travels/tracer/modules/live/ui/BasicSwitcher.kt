package cs10.apps.travels.tracer.modules.live.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.TextSwitcher
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.modules.live.model.SwitcherText

class BasicSwitcher(private var textSwitcher: TextSwitcher, private val autoRepeat: Boolean = true) {
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var lastAction = LastAction.SLIDED_DOWN
    private var ite = 0

    // content to show
    private val mutableList = mutableListOf<SwitcherText>()
    private var lastTextShown: String? = null

    /**
     * Adds content to show using a auto-generated id
     */
    fun addContent(text: String) {
        mutableList.add(SwitcherText(text.hashCode().toString(), text))
    }


    @Deprecated("Use SwitcherText instead of String")
    fun replaceContent(text: String, index: Int){
        val st = SwitcherText(index.toString(), text)

        if (index >= mutableList.size) mutableList.add(st)
        else mutableList[index] = st
    }

    fun replaceContent(text: SwitcherText, preferredIndex: Int = -1){
        val i = mutableList.indexOf(text)

        if (i == -1) {
            if (preferredIndex != -1) mutableList.add(preferredIndex, text)
            else mutableList.add(text)
        } else mutableList[i] = text
    }

    fun start(){
        if (handler != null || mutableList.isEmpty()) return

        handler = Handler(Looper.getMainLooper())
        ite = 0

        runnable = Runnable {
            if (ite < mutableList.size){
                slideUp(mutableList[ite].text)
                ite++
                scheduleNext(16000L / mutableList.size)
            } else {
                slideDown(mutableList[0].text)
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

    private fun getContext() : Context {
        return textSwitcher.context
    }

    private enum class LastAction {
        SLIDED_DOWN, SLIDED_UP
    }
}