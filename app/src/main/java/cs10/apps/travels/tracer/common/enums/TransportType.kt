package cs10.apps.travels.tracer.common.enums

enum class TransportType {
    BUS, TRAIN, CAR, METRO;

    companion object {
        fun fromOrdinal(ordinal: Int): TransportType {
            return when(ordinal) {
                0 -> BUS
                1 -> TRAIN
                2 -> CAR
                3 -> METRO
                else -> BUS
            }
        }
    }
}