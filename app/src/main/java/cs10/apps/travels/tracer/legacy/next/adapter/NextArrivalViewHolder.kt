package cs10.apps.travels.tracer.legacy.next.adapter

import android.view.View
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.joins.ScheduledParada

class NextArrivalViewHolder(view: View) : NextEventViewHolder(view) {

    override fun render(item: ScheduledParada, top : Boolean, onClickListener: (ScheduledParada) -> Unit) {

        // do common render
        super.render(item, top, onClickListener)

        // show destination info
        binding.tvStartCount.text = binding.root.context.getString(R.string.destination, item.nombrePdaFin)
    }
}