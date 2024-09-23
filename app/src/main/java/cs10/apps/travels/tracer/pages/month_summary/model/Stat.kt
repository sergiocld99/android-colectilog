package cs10.apps.travels.tracer.pages.month_summary.model

import kotlin.math.roundToInt

open class Stat(val spent: Double, val porcentage: Int) {

    constructor(spent: Double, total: Double) : this(spent, (spent * 100 / total).roundToInt())
}