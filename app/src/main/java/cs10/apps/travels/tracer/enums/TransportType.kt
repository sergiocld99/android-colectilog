package cs10.apps.travels.tracer.enums

enum class TransportType {
    BUS, TRAIN, CAR;

    companion object {
        fun fromOrdinal(ordinal: Int): TransportType {
            return when(ordinal) {
                0 -> BUS
                1 -> TRAIN
                2 -> CAR
                else -> BUS
            }
        }
    }
}