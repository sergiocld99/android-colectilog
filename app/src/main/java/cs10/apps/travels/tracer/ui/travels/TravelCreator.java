package cs10.apps.travels.tracer.ui.travels;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.List;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.ActivityTravelCreatorBinding;
import cs10.apps.travels.tracer.databinding.ContentTravelCreatorBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ParadasDao;
import cs10.apps.travels.tracer.db.ViajesDao;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.ui.stops.StopCreator;

public class TravelCreator extends AppCompatActivity {
    private ContentTravelCreatorBinding content;
    private ArrayAdapter<? extends Parada> startAdapter, endAdapter;
    private AdapterView.OnItemSelectedListener onStartPlaceSelected, onEndPlaceSelected;
    private FusedLocationProviderClient client;
    private List<Parada> paradas;
    private int startIndex, endIndex;

    private final String[] messages = {
            "Viaje creado con éxito",
            "Por favor complete los campos para continuar",
            "La parada inicial no puede coincidir con la parada final",
            "Formato de hora incorrecto",
            "Formato de fecha incorrecto",
            "Error general de formato",
            "No hay paradas guardadas"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityTravelCreatorBinding binding = ActivityTravelCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        content = binding.contentTravelCreator;

        binding.fab.setOnClickListener(view -> performDone());
        binding.fabStop.setOnClickListener(view -> startActivity(new Intent(this, StopCreator.class)));
        content.tvTitle.setText(getString(R.string.new_travel));

        onStartPlaceSelected = new OnStartPlaceSelected();
        onEndPlaceSelected = new OnEndPlaceSelected();

        // set today values
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        content.etDate.setText(Utils.dateFormat(day, month, year));
        content.etStartHour.setText(Utils.hourFormat(hour, minute));

        client = LocationServices.getFusedLocationProviderClient(this);
        getLocation();
    }

    private void getLocation() throws SecurityException {
        if (Utils.checkPermissions(this)) client.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) loadStopsByDefault();
            else loadStopsByProximity(location);
        });
    }

    private void loadStopsByDefault() {
        new Thread(() -> {
            ParadasDao dao = MiDB.getInstance(this).paradasDao();
            paradas = dao.getAll();
            runOnUiThread(this::setSpinners);
        }, "stopsByDefault").start();
    }

    private void loadStopsByProximity(Location location) {
        new Thread(() -> {
            ParadasDao dao = MiDB.getInstance(this).paradasDao();
            paradas = dao.getAll();
            Utils.orderByProximity(paradas, location.getLatitude(), location.getLongitude());
            runOnUiThread(this::setSpinners);
        }, "stopsByProximity").start();
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

    private void performDone(){
        Viaje viaje = new Viaje();
        int result = onCheckEntries(viaje);

        if (result == 0) new Thread(() -> {
            ViajesDao dao = MiDB.getInstance(this).viajesDao();
            dao.insert(viaje);
            runOnUiThread(this::finish);
        }, "onSaveViaje").start();

        Toast.makeText(getApplicationContext(), messages[result], Toast.LENGTH_LONG).show();
    }

    private int onCheckEntries(@NonNull Viaje viaje){
        if (paradas == null || paradas.isEmpty()) return 6;

        String line = content.etLine.getText().toString();
        String ramal = content.etRamal.getText().toString().trim();
        String date = content.etDate.getText().toString();
        String startHour = content.etStartHour.getText().toString();
        String price = content.etPrice.getText().toString();
        Parada startPlace = paradas.get(startIndex);
        Parada endPlace = paradas.get(endIndex);

        if (date.isEmpty() || startHour.isEmpty()) return 1;
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
            if (!price.isEmpty()) viaje.setCosto(Double.parseDouble(price));
            if (!ramal.isEmpty()) viaje.setRamal(ramal);
            if (line.isEmpty()) viaje.setTipo(1);
            else viaje.setLinea(Integer.parseInt(line));
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
            runOnUiThread(() -> {
                if (maxP != null) content.etPrice.setText(String.valueOf(maxP));
                else content.etPrice.setText(null);
            });
        }).start();
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