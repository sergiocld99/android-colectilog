package cs10.apps.travels.tracer.viewmodel.stats

import kotlin.math.roundToInt

open class Stat(val spent: Double, val porcentage: Int) {

    constructor(spent: Double, total: Double) : this(spent, (spent * 100 / total).roundToInt())
}