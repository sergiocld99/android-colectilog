package cs10.apps.travels.tracer.ui.travels;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class TravelEditor extends AppCompatActivity {
    private ContentTravelCreatorBinding content;
    private ArrayAdapter<Parada> startAdapter, endAdapter;
    private AdapterView.OnItemSelectedListener onStartPlaceSelected, onEndPlaceSelected;
    private List<Parada> paradas;
    private Viaje viaje;
    private int startIndex, endIndex;
    private long travelId;

    private final String[] messages = {
            "Viaje actualizado con éxito",
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
        binding.fabStop.setVisibility(View.GONE);
        content.tvTitle.setText(getString(R.string.edit_travel));

        onStartPlaceSelected = new OnStartPlaceSelected();
        onEndPlaceSelected = new OnEndPlaceSelected();

        // extra
        travelId = getIntent().getLongExtra("travelId", -1);
        if (travelId == -1) return;

        Thread spinnerFiller = new Thread(() -> {
            ParadasDao dao = MiDB.getInstance(this).paradasDao();
            paradas = dao.getAll();

            startAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paradas);
            endAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paradas);

            startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            runOnUiThread(() -> {
                content.selectorStartPlace.setAdapter(startAdapter);
                content.selectorEndPlace.setAdapter(endAdapter);
                content.selectorStartPlace.setOnItemSelectedListener(onStartPlaceSelected);
                content.selectorEndPlace.setOnItemSelectedListener(onEndPlaceSelected);
            });
        }, "stopsSpinnerFiller");

        Thread retrieveTravel = new Thread(() -> {
            viaje = MiDB.getInstance(this).viajesDao().getById(travelId);

            try {
                spinnerFiller.join();
                runOnUiThread(this::retrieve);
            } catch (Exception e){
                e.printStackTrace();
            }
        }, "retrieveTravel");

        spinnerFiller.start();
        retrieveTravel.start();
    }

    private void retrieve() {
        if (viaje.getLinea() != null) content.etLine.setText(String.valueOf(viaje.getLinea()));
        if (viaje.getRamal() != null) content.etRamal.setText(viaje.getRamal());
        if (viaje.getCosto() != 0) content.etPrice.setText(String.valueOf(viaje.getCosto()));
        content.etDate.setText(Utils.dateFormat(viaje.getDay(), viaje.getMonth(), viaje.getYear()));
        content.etStartHour.setText(Utils.hourFormat(viaje.getStartHour(), viaje.getStartMinute()));

        startIndex = getPosFor(viaje.getNombrePdaInicio());
        endIndex = getPosFor(viaje.getNombrePdaFin());

        content.selectorStartPlace.setSelection(startIndex);
        content.selectorEndPlace.setSelection(endIndex);
    }

    private int getPosFor(String stopName){
        int i=0;

        for (Parada p : paradas){
            if (p.getNombre().equals(stopName)) return i;
            i++;
        }

        return 0;
    }

    private void performDone(){
        int result = onCheckEntries(viaje);

        if (result == 0) new Thread(() -> {
            ViajesDao dao = MiDB.getInstance(this).viajesDao();
            dao.update(viaje);
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
            if (!ramal.isEmpty()) viaje.setRamal(ramal);
            if (!price.isEmpty()) viaje.setCosto(Double.parseDouble(price));
            if (line.isEmpty()){
                viaje.setTipo(1);
                viaje.setLinea(null);
            } else {
                viaje.setTipo(0);
                viaje.setLinea(Integer.parseInt(line));
            }
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