package cs10.apps.common.android

abstract class Localizable {
    abstract fun getX(): Double
    abstract fun getY(): Double

    fun kmDistanceTo(other: Localizable) : Double {
        val hyp = NumberUtils.hyp(other.getX() - getX(), other.getY() - getY())
        return NumberUtils.coordsDistanceToKm(hyp)
    }
}