package cs10.apps.travels.tracer.ui.travels;

import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ViajesDao;
import cs10.apps.travels.tracer.model.Viaje;

public abstract class CommonTravelCreator extends AppCompatActivity {

    private final String[] messages = {
            "Viaje creado con éxito",
            "Por favor complete los campos para continuar",
            "La parada inicial no puede coincidir con la parada final",
            "Formato de hora incorrecto",
            "Formato de fecha incorrecto",
            "Error general de formato",
            "No hay paradas guardadas"
    };

    protected void setDoneFabBehavior(@NonNull FloatingActionButton fab){
        fab.setOnClickListener(view -> performDone());
    }

    protected void setCurrentTime(@NonNull EditText etDate, @NonNull EditText etStartHour){
        // set today values
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        etDate.setText(Utils.dateFormat(day, month, year));
        etStartHour.setText(Utils.hourFormat(hour, minute));
    }

    protected String getMessage(int index){
        return messages[index];
    }

    private void performDone(){
        Viaje viaje = new Viaje();
        int result = onCheckEntries(viaje);

        if (result == 0) new Thread(() -> {
            ViajesDao dao = MiDB.getInstance(this).viajesDao();
            dao.insert(viaje);
            runOnUiThread(this::finish);
        }, "onSaveViaje").start();

        Toast.makeText(getApplicationContext(), getMessage(result), Toast.LENGTH_LONG).show();
    }

    abstract int onCheckEntries(Viaje viaje);
}
