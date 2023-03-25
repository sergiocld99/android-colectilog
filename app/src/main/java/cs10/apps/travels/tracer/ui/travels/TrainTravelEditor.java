package cs10.apps.travels.tracer.ui.travels;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.ActivityTrainTravelCreatorBinding;
import cs10.apps.travels.tracer.databinding.ContentTrainTravelCreatorBinding;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Viaje;

public class TrainTravelEditor extends CommonTravelEditor {
    private ContentTrainTravelCreatorBinding content;
    private AdapterView.OnItemSelectedListener onStartPlaceSelected, onEndPlaceSelected;
    private int startIndex, endIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityTrainTravelCreatorBinding binding = ActivityTrainTravelCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        content = binding.content;
        onStartPlaceSelected = new OnStartPlaceSelected();
        onEndPlaceSelected = new OnEndPlaceSelected();

        super.prepare(db -> db.paradasDao().getCustomTrainStops(), content.redSubeHeader);
        super.setFabBehavior(binding.fab);

        Utils.loadTrainBanner(binding.appbarImage);
        binding.toolbarLayout.setTitle(getString(R.string.edit_travel));

        // listeners to open pickers
        content.etDate.setOnClickListener(v -> createDatePicker());

        // disable people count (1 travel = 1 person)
        content.etPeopleCount.setEnabled(false);
    }

    @Override
    public void setSpinners() {
        content.selectorStartPlace.setAdapter(startAdapter);
        content.selectorEndPlace.setAdapter(endAdapter);
        content.selectorStartPlace.setOnItemSelectedListener(onStartPlaceSelected);
        content.selectorEndPlace.setOnItemSelectedListener(onEndPlaceSelected);
    }

    @Override
    public void retrieve() {
        if (viaje.getCosto() != 0) content.etPrice.setText(String.valueOf(viaje.getCosto()));
        content.etDate.setText(Utils.dateFormat(viaje.getDay(), viaje.getMonth(), viaje.getYear()));
        content.etStartHour.setText(Utils.hourFormat(viaje.getStartHour(), viaje.getStartMinute()));

        if (viaje.getEndHour() != null && viaje.getEndMinute() != null){
            content.etEndHour.setText(Utils.hourFormat(viaje.getEndHour(), viaje.getEndMinute()));
        }

        startIndex = getPosFor(viaje.getNombrePdaInicio());
        endIndex = getPosFor(viaje.getNombrePdaFin());

        content.selectorStartPlace.setSelection(startIndex);
        content.selectorEndPlace.setSelection(endIndex);
    }

    private int getPosFor(String stopName) {
        int i = 0;

        for (Parada p : getParadas()) {
            if (p.getNombre().equals(stopName)) return i;
            i++;
        }

        return 0;
    }

    @Override
    public int onCheckEntries(@NonNull Viaje viaje) {
        if (getParadas().isEmpty()) return 6;

        String date = content.etDate.getText().toString();
        String startHour = content.etStartHour.getText().toString();
        String endHour = content.etEndHour.getText().toString();
        String price = content.etPrice.getText().toString();
        Parada startPlace = getParadas().get(startIndex);
        Parada endPlace = getParadas().get(endIndex);

        String[] hourParams, endHourParams = null;

        if (date.isEmpty() || startHour.isEmpty()) return 1;
        if (startPlace.equals(endPlace)) return 2;

        hourParams = startHour.split(":");
        if (hourParams.length != 2) {
            content.etStartHour.setError("Ingrese una hora válida");
            return 3;
        }

        if (!endHour.isEmpty()) {
            endHourParams = endHour.split(":");
            if (endHourParams.length != 2) {
                content.etEndHour.setError("Ingrese una hora válida");
                return 3;
            }
        }

        String[] dateParams = date.split("/");
        if (dateParams.length != 3) {
            content.etDate.setError("Ingrese una fecha válida");
            return 4;
        }

        try {
            viaje.setStartHour(Integer.parseInt(hourParams[0]));
            viaje.setStartMinute(Integer.parseInt(hourParams[1]));
            viaje.setDay(Integer.parseInt(dateParams[0]));
            viaje.setMonth(Integer.parseInt(dateParams[1]));
            viaje.setYear(Integer.parseInt(dateParams[2]));
            viaje.setNombrePdaInicio(startPlace.getNombre());
            viaje.setNombrePdaFin(endPlace.getNombre());
            Utils.setWeekDay(viaje);
            if (!price.isEmpty()) viaje.setCosto(Double.parseDouble(price));

            if (endHourParams == null) {
                viaje.setEndHour(null);
                viaje.setEndMinute(null);
            } else {
                viaje.setEndHour(Integer.parseInt(endHourParams[0]));
                viaje.setEndMinute(Integer.parseInt(endHourParams[1]));
            }

            // train type
            viaje.setTipo(1);
            viaje.setLinea(null);
        } catch (Exception e) {
            e.printStackTrace();
            return 5;
        }

        return 0;
    }

    @Override
    public void onDateSet(int day, int month, int year) {
        content.etDate.setText(Utils.dateFormat(day, month, year));
    }


    private class OnStartPlaceSelected implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            startIndex = i;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class OnEndPlaceSelected implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            endIndex = i;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

}