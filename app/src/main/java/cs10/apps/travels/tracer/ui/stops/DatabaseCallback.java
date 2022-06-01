package cs10.apps.travels.tracer.ui.stops;

import cs10.apps.travels.tracer.db.MiDB;

public interface DatabaseCallback {

    MiDB getInstanceWhenFinished();
}
