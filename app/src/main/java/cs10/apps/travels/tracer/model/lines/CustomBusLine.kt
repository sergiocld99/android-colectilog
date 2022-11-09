package cs10.apps.travels.tracer.model.lines

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lines")
data class CustomBusLine(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val number: Int?,
    val name: String?,
    val color: Int
) : Comparable<CustomBusLine> {

    override fun compareTo(other: CustomBusLine): Int {
        return (number ?: 0).compareTo(other.number ?: 0)
    }
}