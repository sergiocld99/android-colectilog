package cs10.apps.travels.tracer.modules.path.entity

import androidx.room.Entity

@Entity
data class PathGroup(val line: Int?, val ramal: String?, val destination: String, val length: Int){

    override fun toString(): String {
        return "$line $ramal $destination with $length medium stops"
    }
}