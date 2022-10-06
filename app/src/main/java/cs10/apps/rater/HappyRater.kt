package cs10.apps.rater

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.databinding.SimpleRateBinding
import kotlin.math.roundToInt

class HappyRater {
    var doneCallback: (Int) -> Unit = {}
    var cancelCallback: () -> Unit = {}
    private var integerRate = -1

    fun create(context: Context, layoutInflater: LayoutInflater) {
        val view = SimpleRateBinding.inflate(layoutInflater, null, false)

        AlertDialog.Builder(context).let { b ->
            b.setView(view.root)
            b.setPositiveButton("Done!") { dialogInterface, i -> doneCallback(integerRate) }
            b.setNeutralButton("Cancel") { dialogInterface, i -> cancelCallback() }
            b.create().show()
        }

        // emoticon changes
        view.ratingBar.setOnRatingBarChangeListener { bar, rating, b ->
            // update integer rate
            integerRate = rating.roundToInt()

            // update emoticon
            autoUpdateEmoticon(view)
        }
    }

    private fun autoUpdateEmoticon(view: SimpleRateBinding){
        val drawable = when(integerRate){
            5 -> R.drawable.ic_sentiment_very_satisfied
            4 -> R.drawable.ic_sentiment_satisfied_alt
            3 -> R.drawable.ic_sentiment_satisfied
            2 -> R.drawable.ic_sentiment_dissatisfied
            else -> R.drawable.ic_sentiment_very_dissatisfied
        }

        view.emoticon.setImageDrawable(ContextCompat.getDrawable(view.root.context, drawable))
    }
}