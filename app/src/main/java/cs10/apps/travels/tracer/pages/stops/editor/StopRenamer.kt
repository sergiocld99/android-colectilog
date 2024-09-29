package cs10.apps.travels.tracer.pages.stops.editor

import cs10.apps.travels.tracer.db.MiDB

class StopRenamer {

    companion object {

        fun applyChanges(oldName: String, newName: String, db: MiDB) {
            // NOT NECESARY DUE TO FOREIGN KEYS
            // db.travelsDao().renameStartPlaces(oldName, newName)
            // db.travelsDao().renameEndPlaces(oldName, newName)

            db.safeStopsDao().renameDestinations(oldName, newName)
            db.safeStopsDao().renamePrevStops(oldName, newName)
            db.safeStopsDao().renameCurrentStops(oldName, newName)
            db.safeStopsDao().renameNextStops(oldName, newName)
        }
    }
}