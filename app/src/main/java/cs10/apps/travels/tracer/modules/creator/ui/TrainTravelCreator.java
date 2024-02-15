package cs10.apps.travels.tracer.modules.creator.ui;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.data.generator.Station;
import cs10.apps.travels.tracer.data.generator.FareData;
import cs10.apps.travels.tracer.databinding.ActivityTrainTravelCreatorBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ParadasDao;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Viaje;

public class TrainTravelCreator extends CommonTravelCreator {
    private ActivityTrainTravelCreatorBinding binding;
    private ArrayAdapter<? extends Parada> startAdapter, endAdapter;
    private AdapterView.OnItemSelectedListener onStartPlaceSelected, onEndPlaceSelected;
    private FusedLocationProviderClient client;
    private List<Parada> paradas;
    private int startIndex, endIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTrainTravelCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Utils.loadTrainBanner(binding.appbarImage);
        binding.toolbarLayout.setTitle(getString(R.string.new_train_travel));

        onStartPlaceSelected = new OnStartPlaceSelected();
        onEndPlaceSelected = new OnEndPlaceSelected();

        // default init config
        super.setDoneFabBehavior(binding.fab);
        super.setCurrentTime(binding.content.etDate, binding.content.etStartHour, binding.content.redSubeHeader);
        binding.content.etEndHour.setEnabled(false);

        // order stops by last location
        client = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        // listeners to open pickers
        binding.content.etDate.setOnClickListener(v -> createDatePicker());
    }

    private void getLocation() throws SecurityException {
        if (Utils.checkPermissions(this)) client.getLastLocation().addOnSuccessListener(this::loadStops);
    }

    private void loadStops(Location location){
        new Thread(() -> {
            ParadasDao dao = MiDB.getInstance(this).paradasDao();
            paradas = dao.getCustomTrainStops();
            if (location != null) Utils.orderByProximity(paradas, location.getLatitude(), location.getLongitude());
            runOnUiThread(this::setSpinners);
        }, "loadTrainStops").start();
    }

    private void setSpinners(){
        startAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paradas);
        endAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paradas);

        startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.content.selectorStartPlace.setAdapter(startAdapter);
        binding.content.selectorEndPlace.setAdapter(endAdapter);
        binding.content.selectorStartPlace.setOnItemSelectedListener(onStartPlaceSelected);
        binding.content.selectorEndPlace.setOnItemSelectedListener(onEndPlaceSelected);
    }

    @Override
    public int onCheckEntries(@NonNull Viaje viaje){
        if (paradas == null || paradas.isEmpty()) return 6;

        String date = binding.content.etDate.getText().toString().trim();
        String startHour = binding.content.etStartHour.getText().toString().trim();
        String peopleCount = binding.content.etPeopleCount.getText().toString().trim();
        String price = binding.content.etPrice.getText().toString().replace("$","").trim();
        Parada startPlace = paradas.get(startIndex);
        Parada endPlace = paradas.get(endIndex);

        if (date.isEmpty() || startHour.isEmpty() || peopleCount.isEmpty()) return 1;
        if (startPlace.equals(endPlace)) return 2;

        String[] hourParams = startHour.split(":");
        if (hourParams.length != 2){
            binding.content.etStartHour.setError("Ingrese una hora válida");
            return 3;
        }

        String[] dateParams = date.split("/");
        if (dateParams.length != 3){
            binding.content.etDate.setError("Ingrese una fecha válida");
            return 4;
        }

        try {
            viaje.setTipo(1);
            viaje.setStartHour(Integer.parseInt(hourParams[0]));
            viaje.setStartMinute(Integer.parseInt(hourParams[1]));
            viaje.setDay(Integer.parseInt(dateParams[0]));
            viaje.setMonth(Integer.parseInt(dateParams[1]));
            viaje.setYear(Integer.parseInt(dateParams[2]));
            viaje.setNombrePdaInicio(startPlace.getNombre());
            viaje.setNombrePdaFin(endPlace.getNombre());
            viaje.setPeopleCount(Integer.parseInt(peopleCount));
            if (viaje.getPeopleCount() <= 0 || viaje.getPeopleCount() >= 10) return 7;
            Utils.setWeekDay(viaje);
            if (!price.isEmpty()) viaje.setCosto(Double.parseDouble(price));
        } catch (Exception e){
            e.printStackTrace();
            return 5;
        }

        return 0;
    }

    public void updatePrice(){
        if (paradas != null && !paradas.isEmpty()) {
            Station s1 = Station.findByNombre(paradas.get(startIndex).getNombre());
            Station s2 = Station.findByNombre(paradas.get(endIndex).getNombre());

            if (s1 != null && s2 != null){
                // use tarifa data
                FareData data = new FareData();
                double price = data.getTarifa(s1, s2);
                binding.content.etPrice.setText(Utils.priceFormat(price));
            } else binding.content.etPrice.setText(null);
        }
    }

    @Override
    public void onDateSet(int day, int month, int year) {
        binding.content.etDate.setText(Utils.dateFormat(day, month, year));
    }

    private class OnStartPlaceSelected implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            startIndex = i;
            updatePrice();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class OnEndPlaceSelected implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            endIndex = i;
            updatePrice();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

}