package cs10.apps.travels.tracer.adapter;

public interface EditTravelCallback extends AdapterCallback {
    void onDeleteTravel(long travelId, int pos);
    void onEditTravel(long id);
}
