package cs10.apps.travels.tracer.ui.travels;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;

import java.util.List;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.ActivityTrainTravelCreatorBinding;
import cs10.apps.travels.tracer.databinding.ContentTrainTravelCreatorBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Viaje;
import kotlin.jvm.functions.Function1;

public class TrainTravelEditor extends CommonTravelEditor {
    private ContentTrainTravelCreatorBinding myContent;
    private AdapterView.OnItemSelectedListener onStartPlaceSelected, onEndPlaceSelected;
    private int startIndex, endIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityTrainTravelCreatorBinding binding = ActivityTrainTravelCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        myContent = binding.content;
        onStartPlaceSelected = new OnStartPlaceSelected();
        onEndPlaceSelected = new OnEndPlaceSelected();

        super.prepare(db -> db.paradasDao().getCustomTrainStops());
        super.setFabBehavior(binding.fab);

        Utils.loadTrainBanner(binding.appbarImage);
        binding.toolbarLayout.setTitle(getString(R.string.edit_travel));
    }

    @Override
    public void setSpinners() {
        myContent.selectorStartPlace.setAdapter(startAdapter);
        myContent.selectorEndPlace.setAdapter(endAdapter);
        myContent.selectorStartPlace.setOnItemSelectedListener(onStartPlaceSelected);
        myContent.selectorEndPlace.setOnItemSelectedListener(onEndPlaceSelected);
    }

    @Override
    public void retrieve() {
        if (viaje.getCosto() != 0) myContent.etPrice.setText(String.valueOf(viaje.getCosto()));
        myContent.etDate.setText(Utils.dateFormat(viaje.getDay(), viaje.getMonth(), viaje.getYear()));
        myContent.etStartHour.setText(Utils.hourFormat(viaje.getStartHour(), viaje.getStartMinute()));

        startIndex = getPosFor(viaje.getNombrePdaInicio());
        endIndex = getPosFor(viaje.getNombrePdaFin());

        myContent.selectorStartPlace.setSelection(startIndex);
        myContent.selectorEndPlace.setSelection(endIndex);
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

        String date = myContent.etDate.getText().toString();
        String startHour = myContent.etStartHour.getText().toString();
        String price = myContent.etPrice.getText().toString();
        Parada startPlace = getParadas().get(startIndex);
        Parada endPlace = getParadas().get(endIndex);

        if (date.isEmpty() || startHour.isEmpty()) return 1;
        if (startPlace.equals(endPlace)) return 2;

        String[] hourParams = startHour.split(":");
        if (hourParams.length != 2){
            myContent.etStartHour.setError("Ingrese una hora válida");
            return 3;
        }

        String[] dateParams = date.split("/");
        if (dateParams.length != 3){
            myContent.etDate.setError("Ingrese una fecha válida");
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

            // train type
            viaje.setTipo(1);
            viaje.setLinea(null);
        } catch (Exception e){
            e.printStackTrace();
            return 5;
        }

        return 0;
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