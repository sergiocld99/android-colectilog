package cs10.apps.travels.tracer.ui.stops;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.databinding.ActivityStopCreatorBinding;
import cs10.apps.travels.tracer.databinding.ContentStopCreatorBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ParadasDao;
import cs10.apps.travels.tracer.model.Parada;

public class StopCreator extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private ContentStopCreatorBinding content;
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
        content.tvTitle.setText(getString(R.string.new_stop));

        // Selector
        String[] options = {"Colectivo", "Tren"};
        ArrayAdapter<String> aa = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, options);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        content.selectorType.setAdapter(aa);
        content.selectorType.setOnItemSelectedListener(this);
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
        if (dao.getByName(stopName) != null) return 2;

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