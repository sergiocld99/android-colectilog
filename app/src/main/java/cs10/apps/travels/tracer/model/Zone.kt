package cs10.apps.travels.tracer.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Zone(
    var name: String,
    var x0: Double,
    var x1: Double,
    var y0: Double,
    var y1: Double
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    fun getCenterX() : Double {
        return 0.5 * (x0 + x1)
    }

    fun getCenterY() : Double {
        return 0.5 * (y0 + y1)
    }
}
