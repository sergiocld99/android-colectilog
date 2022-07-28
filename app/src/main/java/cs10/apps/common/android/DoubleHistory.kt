package cs10.apps.common.android

class DoubleHistory(private val minDiff: Double) {

    private var status: Int = 0
    private var prev: Double? = null
    var current: Double? = null
    var diffSum: Double = 0.0

    fun updateNew(newValue: Double){
        when {
            prev == null -> {
                status = 0
                prev = current
            }
            newValue > prev!! * (1+minDiff) -> {
                status = 1
                if (diffSum > 0) diffSum += newValue - prev!!
                else diffSum = newValue - prev!!
                prev = current
            }
            newValue < prev!! * (1-minDiff) -> {
                status = 2
                if (diffSum < 0) diffSum -= (prev!! - newValue)
                else diffSum = newValue - prev!!
                prev = current
            }
        }

        current = newValue
    }

    fun currentIsGreater(): Boolean {
        return status == 1
    }

    fun currentIsLess(): Boolean {
        return status == 2
    }

    fun comparisonDiff(): Double {
        return when {
            current == null -> 0.0
            diffSum >= 0.0 -> current!!
            else -> diffSum / current!!
        }
    }
}