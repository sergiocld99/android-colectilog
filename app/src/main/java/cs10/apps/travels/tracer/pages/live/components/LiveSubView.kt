package cs10.apps.travels.tracer.pages.live.components

import android.content.Context
import android.view.View
import androidx.core.view.isVisible

abstract class LiveSubView(private val rootView: View) {

    open fun hide(){
        rootView.isVisible = false
    }

    open fun show(){
        rootView.isVisible = true
    }

    fun isVisible() = rootView.isVisible
    fun getContext(): Context = rootView.context
}