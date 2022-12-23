package cs10.apps.travels.tracer.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import cs10.apps.travels.tracer.model.joins.ZoneStats

@Entity
class Zone(
    var name: String,
    var x0: Double,
    var x1: Double,
    var y0: Double,
    var y1: Double
) : Comparable<Zone> {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @Ignore
    var zoneStats: ZoneStats? = null

    fun getCenterX() : Double {
        return 0.5 * (x0 + x1)
    }

    fun getCenterY() : Double {
        return 0.5 * (y0 + y1)
    }

    override fun compareTo(other: Zone): Int {
        val otherCount = other.zoneStats?.travelsCount?: 0
        val thisCount = this.zoneStats?.travelsCount?: 0

        return otherCount.compareTo(thisCount)
    }
}
