package cs10.apps.rater

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import cs10.apps.travels.tracer.databinding.SimpleRateBinding

class HappyRater {

    fun create(context: Context, layoutInflater: LayoutInflater) {
        val view = SimpleRateBinding.inflate(layoutInflater, null, false)

        AlertDialog.Builder(context).let { b ->
            b.setView(view.root)
            b.setPositiveButton("Done!", null)
            b.setNegativeButton("Cancel", null)
            b.create().show()
        }
    }
}