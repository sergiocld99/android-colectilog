package cs10.apps.travels.tracer.ui.travels;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.List;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.ActivityTravelCreatorBinding;
import cs10.apps.travels.tracer.databinding.ContentBusTravelCreatorBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ViajesDao;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.modules.RedSube;
import cs10.apps.travels.tracer.ui.stops.StopCreator;

public class BusTravelCreator extends CommonTravelCreator {
    private ContentBusTravelCreatorBinding content;
    private ArrayAdapter<? extends Parada> startAdapter, endAdapter;
    private ArrayAdapter<String> ramalAdapter;
    private AdapterView.OnItemSelectedListener onStartPlaceSelected, onEndPlaceSelected;
    private FusedLocationProviderClient client;
    private List<Parada> paradas;
    private int startIndex, endIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityTravelCreatorBinding binding = ActivityTravelCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Utils.loadBusBanner(binding.appbarImage);
        binding.toolbarLayout.setTitle(getString(R.string.new_travel));
        content = binding.contentTravelCreator;

        onStartPlaceSelected = new OnStartPlaceSelected();
        onEndPlaceSelected = new OnEndPlaceSelected();

        // default config init
        super.setDoneFabBehavior(binding.fab);
        super.setCurrentTime(content.etDate, content.etStartHour, content.redSubeHeader);
        content.etEndHour.setEnabled(false);

        // hint values
        autoFillRamals();

        // order stops by last location
        client = LocationServices.getFusedLocationProviderClient(this);
        getLocation();

        // listeners
        content.etDate.setOnClickListener(v -> createDatePicker());
        binding.fabStop.setOnClickListener(view -> startActivity(new Intent(this, StopCreator.class)));
    }

    private void autoFillRamals() {
        doInBackground(() -> {
            List<String> ramals = MiDB.getInstance(this).viajesDao().getAllRamals();

            doInForeground(() -> {
                ramalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ramals);
                content.etRamal.setAdapter(ramalAdapter);
            });
        });
    }

    private void getLocation() throws SecurityException {
        if (Utils.checkPermissions(this)) client.getLastLocation().addOnSuccessListener(this::loadStops);
    }

    private void loadStops(Location location) {
        doInBackground(() -> {
            MiDB db = MiDB.getInstance(this);
            paradas = db.paradasDao().getAll();
            if (location != null) Utils.orderByProximity(paradas, location.getLatitude(), location.getLongitude());
            doInForeground(this::setSpinners);

            // part 2: autocomplete likely travel
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            if (!paradas.isEmpty()){
                Viaje viaje = db.viajesDao().getLikelyTravelFrom(paradas.get(0).getNombre(), currentHour);
                if (viaje != null) runOnUiThread(() -> autoFillLikelyTravel(viaje));
            }

        });
    }

    private void autoFillLikelyTravel(@NonNull Viaje viaje){
        if (viaje.getLinea() != null) content.etLine.setText(String.valueOf(viaje.getLinea()));
        if (viaje.getRamal() != null) content.etRamal.setText(viaje.getRamal());

        // find end index
        int endIndex = 0;

        for (Parada p : paradas){
            if (p.getNombre().equals(viaje.getNombrePdaFin())) break;
            else endIndex++;
        }

        content.selectorEndPlace.setSelection(endIndex, true);
    }

    private void setSpinners(){
        startAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paradas);
        endAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paradas);

        startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        content.selectorStartPlace.setAdapter(startAdapter);
        content.selectorEndPlace.setAdapter(endAdapter);
        content.selectorStartPlace.setOnItemSelectedListener(onStartPlaceSelected);
        content.selectorEndPlace.setOnItemSelectedListener(onEndPlaceSelected);
    }

    @Override
    public int onCheckEntries(@NonNull Viaje viaje){
        if (paradas == null || paradas.isEmpty()) return 6;

        String line = content.etLine.getText().toString().trim();
        String ramal = content.etRamal.getText().toString().trim();
        String date = content.etDate.getText().toString().trim();
        String startHour = content.etStartHour.getText().toString().trim();
        String peopleCount = content.etPeopleCount.getText().toString().trim();
        String price = content.etPrice.getText().toString().trim();
        Parada startPlace = paradas.get(startIndex);
        Parada endPlace = paradas.get(endIndex);

        if (date.isEmpty() || startHour.isEmpty() || line.isEmpty() || peopleCount.isEmpty()) return 1;
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

        try {
            viaje.setStartHour(Integer.parseInt(hourParams[0]));
            viaje.setStartMinute(Integer.parseInt(hourParams[1]));
            viaje.setDay(Integer.parseInt(dateParams[0]));
            viaje.setMonth(Integer.parseInt(dateParams[1]));
            viaje.setYear(Integer.parseInt(dateParams[2]));
            viaje.setNombrePdaInicio(startPlace.getNombre());
            viaje.setNombrePdaFin(endPlace.getNombre());
            Utils.setWeekDay(viaje);
            viaje.setLinea(Integer.parseInt(line));
            viaje.setPeopleCount(Integer.parseInt(peopleCount));
            if (viaje.getPeopleCount() <= 0 || viaje.getPeopleCount() >= 10) return 7;
            if (!price.isEmpty()) viaje.setCosto(Double.parseDouble(price));
            if (!ramal.isEmpty()) viaje.setRamal(ramal);
            if (content.ratingBar.getRating() > 0) viaje.setRate(Math.round(content.ratingBar.getRating()));
        } catch (Exception e){
            e.printStackTrace();
            return 5;
        }

        return 0;
    }

    public void updatePrice(){
        if (paradas != null && !paradas.isEmpty()) new Thread(() -> {
            ViajesDao dao = MiDB.getInstance(getApplicationContext()).viajesDao();
            Double maxP = dao.getMaxPrice(paradas.get(startIndex).getNombre(), paradas.get(endIndex).getNombre());

            doInForeground(() -> {
                if (maxP != null) {
                    final double price = maxP * RedSube.Companion.getPercentageToPay(getRedSubeCount()) / 100;
                    content.etPrice.setText(String.valueOf(price));
                } else content.etPrice.setText(null);
            });
        }).start();
    }

    @Override
    public void onDateSet(int day, int month, int year) {
        content.etDate.setText(Utils.dateFormat(day, month, year));
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