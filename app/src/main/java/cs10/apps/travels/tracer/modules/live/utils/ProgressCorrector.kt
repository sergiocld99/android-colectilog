package cs10.apps.travels.tracer.modules.live.utils

import cs10.apps.common.android.Localizable
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.enums.TransportType
import cs10.apps.travels.tracer.model.Viaje
import kotlin.math.pow

class ProgressCorrector {

    fun correct(start: Localizable, end: Localizable, prog: Double, t: Viaje): Double {
        if (t.tipo == TransportType.CAR.ordinal || (t.linea != null && t.linea!! <= 200)) {
            return prog
        }

        return when (Utils.getDirection(start, end)) {
            Utils.Direction.SOUTH_WEST -> f4SO(prog)
            Utils.Direction.SOUTH_EAST -> f5SE(prog, t.ramal, t.tipo)
            Utils.Direction.NORTH_WEST -> f4NW(prog, t.tipo)
            else -> prog
        }
    }

    private fun p3(x: Double, b: Double, c: Double, d: Double, e: Double) : Double {
        return b * x.pow(3) + c * x.pow(2) + d * x + e
    }

    private fun p4(x: Double, a: Double, b: Double, c: Double, d: Double, e: Double) : Double {
        return a * x.pow(4) + b * x.pow(3) + c * x.pow(2) + d * x + e
    }

    private fun f0SE(prog: Double) = 2 * prog.pow(3) - 2.76 * prog.pow(2) + 1.76 * prog
    private fun f0NW(prog: Double) = 0.25 * prog.pow(3) - 1.05 * prog.pow(2) + 1.8 * prog

    // Al sudeste, ramal nulo o belgrano, o bien, en el ultimo tramo
    private fun f1SET(prog: Double) : Double {
        return when {
            prog < 0.3 -> p4(prog, -1.8, 14.6, -6.25, 1.46, 0.0)
            prog > 0.96 -> -241.38 * prog.pow(2) + 479.55 * prog - 237.17
            else -> p4(prog, 1.69, -4.37, 4.82, -1.68, 0.41)
        }
    }

    // feb 10: added roca la plata to varela (f5se opposite)
    private fun f4NW(prog: Double, type: Int) : Double {
        return if (type == TransportType.BUS.ordinal)
            p4(prog, -1.78, 5.43, -5.29, 2.59, 0.03)
        else p3(prog, 1.25, -1.0, 0.75, 0.0)
    }

    private fun f4SO(prog: Double) = p4(prog, -5.46, 9.84, -4.44, 1.05, 0.0)

    // june 23 correction: belgrano vs centenario, and after plaza italia
    private fun f4SE(prog: Double, ramal: String?) : Double {
        return if (ramal == null || !ramal.startsWith("C") || prog > 0.96) f1SET(prog)
        else return p4(prog, 3.0, -4.18, 1.52, 0.62, 0.0)
    }

    // feb 10: added roca varela to la plata
    private fun f5SE(prog: Double, ramal: String?, type: Int) : Double {
        return if (type == TransportType.BUS.ordinal) f4SE(prog, ramal)
        else p3(prog, 1.25, -2.75, 2.5, 0.0)
    }
}