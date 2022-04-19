package cs10.apps.travels.tracer.ui.stops;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.databinding.ActivityStopCreatorBinding;
import cs10.apps.travels.tracer.databinding.ContentStopCreatorBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ParadasDao;
import cs10.apps.travels.tracer.model.Parada;

public class StopEditor extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private ContentStopCreatorBinding content;
    private ParadasDao dao;
    private String originalName;
    private int type;

    private final String[] messages = {
            "Parada actualizada con éxito",
            "Por favor complete todos los campos para continuar",
            "El nombre de parada no se puede cambiar",
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
        content.tvTitle.setText(getString(R.string.editing_stop));

        originalName = getIntent().getExtras().getString("stopName");
        content.etStopName.setText(originalName);

        new Thread(() -> {
            dao = MiDB.getInstance(getApplicationContext()).paradasDao();
            Parada parada = dao.getByName(originalName);
            runOnUiThread(() -> {
                content.etLatitude.setText(String.valueOf(parada.getLatitud()));
                content.etLongitude.setText(String.valueOf(parada.getLongitud()));
                content.selectorType.setSelection(parada.getTipo());
            });
        }).start();

        // Selector
        String[] options = {"Colectivo", "Tren"};
        ArrayAdapter<String> aa = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, options);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        content.selectorType.setAdapter(aa);
        content.selectorType.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete){
            new Thread(() -> {
                dao.delete(originalName);
                runOnUiThread(this::finish);
            }).start();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void performDone(){
        int result = onUpdateStop();

        runOnUiThread(() -> {
            if (result == 0) finish();
            Toast.makeText(getApplicationContext(), messages[result], Toast.LENGTH_LONG).show();
        });
    }

    private int onUpdateStop() {
        String stopName = content.etStopName.getText().toString().trim();
        String latitude = content.etLatitude.getText().toString();
        String longitude = content.etLongitude.getText().toString();

        if (stopName.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) return 1;

        try {
            Parada parada = new Parada();
            parada.setNombre(stopName);
            parada.setLatitud(Double.parseDouble(latitude));
            parada.setLongitud(Double.parseDouble(longitude));
            parada.setTipo(type);

            if (originalName.equals(stopName)) dao.update(parada);
            else {
                dao.delete(originalName);
                dao.insert(parada);
            }
        } catch(NumberFormatException e){
            e.printStackTrace();
            return 3;
        } catch (Exception e){
            return 4;
        }

        return 0;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Cambios descartados", Toast.LENGTH_LONG).show();
        super.onBackPressed();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        type = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}