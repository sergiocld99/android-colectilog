package cs10.apps.travels.tracer.modules.editor.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.ActivityBusTravelEditorBinding;
import cs10.apps.travels.tracer.databinding.ContentBusTravelCreatorBinding;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Viaje;

public class BusTravelEditor extends CommonTravelEditor {
    private ContentBusTravelCreatorBinding content;
    private ActivityBusTravelEditorBinding binding;
    private AdapterView.OnItemSelectedListener onStartPlaceSelected, onEndPlaceSelected;
    private int startIndex, endIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBusTravelEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        content = binding.contentTravelCreator;
        onStartPlaceSelected = new OnStartPlaceSelected();
        onEndPlaceSelected = new OnEndPlaceSelected();

        super.prepare(db -> db.paradasDao().getAll(), content.redSubeHeader);
        super.setFabBehavior(binding.fab);

        Utils.loadBusBanner(binding.appbarImage);
        binding.fabStop.setVisibility(View.GONE);
        binding.toolbarLayout.setTitle(getString(R.string.edit_travel));

        // listeners to open pickers
        content.etDate.setOnClickListener(v -> createDatePicker());

        // disable people count (1 travel = 1 person)
        content.etPeopleCount.setEnabled(false);

        // disable price options
        content.priceOptions.setVisibility(View.GONE);
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
        Viaje viaje = getViaje();
        if (viaje.getLinea() != null) content.etLine.setText(String.valueOf(viaje.getLinea()));
        if (viaje.getRamal() != null) content.etRamal.setText(viaje.getRamal());
        if (viaje.getCosto() != 0) content.etPrice.setText(String.valueOf(viaje.getCosto()));
        if (viaje.getEndHour() != null && viaje.getEndMinute() != null)
            content.etEndHour.setText(Utils.hourFormat(viaje.getEndHour(), viaje.getEndMinute()));

        if (viaje.getRate() != null) content.ratingBar.setRating(viaje.getRate());

        // mandatory
        content.etDate.setText(Utils.dateFormat(viaje.getDay(), viaje.getMonth(), viaje.getYear()));
        content.etStartHour.setText(Utils.hourFormat(viaje.getStartHour(), viaje.getStartMinute()));

        startIndex = getPosFor(viaje.getNombrePdaInicio());
        endIndex = getPosFor(viaje.getNombrePdaFin());

        content.selectorStartPlace.setSelection(startIndex);
        content.selectorEndPlace.setSelection(endIndex);

        // top card
        binding.distanceText.setText(getString(R.string.linear_distance_km, getMt().getDistanceKm()));
        binding.speedText.setText(getString(R.string.speed_kmh, getMt().getSpeedKmH()));
    }

    private int getPosFor(String stopName){
        int i=0;

        for (Parada p : getParadas()){
            if (p.getNombre().equals(stopName)) return i;
            i++;
        }

        return 0;
    }

    @Override
    public int onCheckEntries(@NonNull Viaje viaje){
        if (getParadas().isEmpty()) return 6;

        String line = content.etLine.getText().toString().trim();
        String ramal = content.etRamal.getText().toString().trim();
        String date = content.etDate.getText().toString().trim();
        String startHour = content.etStartHour.getText().toString().trim();
        String endHour = content.etEndHour.getText().toString().trim();
        String price = content.etPrice.getText().toString().trim();
        Parada startPlace = getParadas().get(startIndex);
        Parada endPlace = getParadas().get(endIndex);

        if (line.isEmpty() || date.isEmpty() || startHour.isEmpty()) return 1;
        if (startPlace.equals(endPlace)) return 2;

        String[] hourParams = startHour.split(":");
        if (hourParams.length != 2){
            content.etStartHour.setError("Ingrese una hora válida");
            return 3;
        }

        String[] dateParams = date.split("/");
        if (dateParams.length != 3){
            content.etDate.setError("Ingrese una fecha válida");
            return 4;
        }

        // end hour and minute
        if (endHour.isEmpty()){
            viaje.setEndHour(null);
            viaje.setEndMinute(null);
        } else {
            String[] endHourParams = endHour.split(":");
            if (endHourParams.length != 2){
                content.etEndHour.setError("Ingrese una hora válida");
                return 3;
            }

            try {
                viaje.setEndHour(Integer.parseInt(endHourParams[0]));
                viaje.setEndMinute(Integer.parseInt(endHourParams[1]));
            } catch (NumberFormatException e){
                e.printStackTrace();
                return 5;
            }
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
            viaje.setRamal(ramal.isEmpty() ? null : ramal);
            viaje.setCosto(price.isEmpty() ? 0 : Double.parseDouble(price));
            viaje.setLinea(Integer.parseInt(line));

            if (content.ratingBar.getRating() > 0) viaje.setRate(Math.round(content.ratingBar.getRating()));
            else viaje.setRate(0);

            viaje.setTipo(0);
        } catch (Exception e){
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