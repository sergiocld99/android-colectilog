package cs10.apps.travels.tracer.modules.live.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
        return "{$prev > $name > $next}"
    }
}
