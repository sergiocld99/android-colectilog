package cs10.apps.travels.tracer.pages.stops.editor;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import cs10.apps.common.android.ui.CSActivity;
import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.common.constants.ResultCodes;
import cs10.apps.travels.tracer.common.enums.TransportType;
import cs10.apps.travels.tracer.databinding.ActivityStopCreatorBinding;
import cs10.apps.travels.tracer.databinding.ContentStopCreatorBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Parada;

public class StopEditor extends CSActivity implements AdapterView.OnItemSelectedListener {

    private ContentStopCreatorBinding content;
    private MiDB db;
    private String originalName;
    private int type;

    private final String[] messages = {
            "Parada actualizada con éxito",
            "Por favor complete todos los campos para continuar",
            "El nombre propuesto ya se encuentra en uso",
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

        binding.fab.setOnClickListener(view -> doInBackground(this::performDone));
        content.tvTitle.setText(getString(R.string.editing_stop));

        originalName = getIntent().getExtras().getString("stopName");
        content.etStopName.setText(originalName);

        doInBackground(() -> {
            db = MiDB.getInstance(getApplicationContext());
            Parada parada = db.paradasDao().getByName(originalName);

            // rank
            int travelCount = db.paradasDao().getTravelCount(originalName);
            int rank = db.paradasDao().getRank(travelCount);

            doInForeground(() -> {
                content.etLatitude.setText(String.valueOf(parada.getLatitud()));
                content.etLongitude.setText(String.valueOf(parada.getLongitud()));
                content.selectorType.setSelection(parada.getTipo());
                content.stopSummary.travelCount.setText(getString(R.string.x_travels_done, travelCount));
                content.stopSummary.stopRank.setText(getString(R.string.number_x_in_ranking, rank));
                content.stopSummary.getRoot().setVisibility(View.VISIBLE);
            });
        });

        // Selector
        String[] options = TransportType.Companion.getTypesStr(this);
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
            doInBackground(this::onDelete);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onDelete(){
        db.paradasDao().delete(originalName);
        finishWithResult(ResultCodes.STOP_DELETED);
    }

    private void performDone(){
        int result = onUpdateStop();

        doInForeground(() -> {
            if (result == 0) finish();
            showShortToast(messages[result]);
        });
    }

    private int onUpdateStop() {
        String stopName = content.etStopName.getText().toString().trim();
        String latitude = content.etLatitude.getText().toString();
        String longitude = content.etLongitude.getText().toString();

        if (stopName.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) return 1;

        try {
            Parada parada = new Parada();
            parada.setNombre(originalName);
            parada.setLatitud(Double.parseDouble(latitude));
            parada.setLongitud(Double.parseDouble(longitude));
            parada.setTipo(type);

            if (originalName.equals(stopName)) db.paradasDao().update(parada);
            else {
                // check if the new name already exists
                if (db.paradasDao().getByName(stopName) != null) return 2;

                parada.setNombre(stopName);
                db.safeStopsDao().renameStop(originalName, stopName);
                StopRenamer.Companion.applyChanges(originalName, stopName, db);
                setResult(ResultCodes.STOP_RENAMED);
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
        showLongToast("Cambios descartados");
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