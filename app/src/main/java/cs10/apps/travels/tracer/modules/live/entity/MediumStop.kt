package cs10.apps.travels.tracer.modules.live.entity

import androidx.room.Entity

@Entity(primaryKeys = ["id"])
data class MediumStop(
    val id: Long,
    val type: Int,
    val line: Int?,
    val ramal: String?,
    val prev: String,
    val name: String,
    val next: String,
    val destination: String
)
