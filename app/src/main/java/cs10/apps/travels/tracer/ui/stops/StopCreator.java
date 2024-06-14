package cs10.apps.travels.tracer.ui.stops;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import cs10.apps.common.android.ui.CSActivity;
import cs10.apps.travels.tracer.MapViewActivity;
import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.ActivityStopCreatorBinding;
import cs10.apps.travels.tracer.databinding.ContentStopCreatorBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ParadasDao;
import cs10.apps.travels.tracer.model.Parada;

public class StopCreator extends CSActivity implements AdapterView.OnItemSelectedListener {
    private ContentStopCreatorBinding content;
    private FusedLocationProviderClient client;
    private int type;

    private final String[] messages = {
            "Parada creada con éxito",
            "Por favor complete todos los campos para continuar",
            "El nombre de parada ya se encuentra en uso",
            "Formato inválido de latitud y/o longitud",
            "Hubo un problema al escribir en la base de datos"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityStopCreatorBinding binding = ActivityStopCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        content = binding.contentStopCreator;

        binding.fab.setOnClickListener(view -> new Thread(this::performDone, "performDone").start());
        binding.fabOpenMap.setOnClickListener(v -> onOpenMap());
        content.tvTitle.setText(getString(R.string.new_stop));

        // Selector
        String[] options = {getString(R.string.bus), getString(R.string.train), getString(R.string.car), getString(R.string.metro)};
        ArrayAdapter<String> aa = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, options);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        content.selectorType.setAdapter(aa);
        content.selectorType.setOnItemSelectedListener(this);

        client = LocationServices.getFusedLocationProviderClient(this);
        getLocation();
    }

    private void onOpenMap() {
        String latitude = content.etLatitude.getText().toString().trim();
        String longitude = content.etLongitude.getText().toString().trim();
        String stopName = content.etStopName.getText().toString().trim();

        try {
            double x = Double.parseDouble(latitude);
            double y = Double.parseDouble(longitude);
            Intent intent = new Intent(this, MapViewActivity.class);
            intent.putExtra("lat", x);
            intent.putExtra("long", y);
            intent.putExtra("name", stopName);
            startActivity(intent);
        } catch (NumberFormatException e){
            showShortToast("No se ingresaron coordenadas válidas");
        }
    }

    private void getLocation() throws SecurityException {
        if (Utils.checkPermissions(this)) client.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) return;
            content.etLatitude.setText(String.valueOf(location.getLatitude()));
            content.etLongitude.setText(String.valueOf(location.getLongitude()));
        });
    }

    private void performDone(){
        int result = onCreateStop();

        runOnUiThread(() -> {
            if (result == 0) finish();
            Toast.makeText(getApplicationContext(), messages[result], Toast.LENGTH_LONG).show();
        });
    }

    private int onCreateStop(){
        String stopName = content.etStopName.getText().toString().trim();
        String latitude = content.etLatitude.getText().toString();
        String longitude = content.etLongitude.getText().toString();

        if (stopName.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) return 1;

        ParadasDao dao = MiDB.getInstance(getApplicationContext()).paradasDao();
        if (dao.getByName(stopName) != null) {
            runOnUiThread(() -> content.etStopName.setError("Intente con otro nombre"));
            return 2;
        }

        try {
            Parada parada = new Parada();
            parada.setNombre(stopName);
            parada.setLatitud(Double.parseDouble(latitude));
            parada.setLongitud(Double.parseDouble(longitude));
            parada.setTipo(type);
            dao.insert(parada);
        } catch(NumberFormatException e){
            e.printStackTrace();
            return 3;
        } catch (Exception e){
            return 4;
        }

        return 0;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        type = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}