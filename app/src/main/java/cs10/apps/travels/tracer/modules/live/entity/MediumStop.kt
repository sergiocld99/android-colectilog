package cs10.apps.travels.tracer.modules.live.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cs10.apps.travels.tracer.enums.TransportType

@Entity(tableName = "MediumStop")
data class MediumStop(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: Int,
    val line: Int?,
    val ramal: String?,
    var prev: String,
    val name: String,
    var next: String,
    val destination: String
) {

    override fun toString(): String {
        return " ${TransportType.fromOrdinal(type)} $line $ramal to $destination: {$prev > $name > $next}"
    }
}
