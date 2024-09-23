package cs10.apps.travels.tracer.pages.coffee;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Calendar;

import cs10.apps.common.android.ui.FormActivity;
import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.utils.Utils;
import cs10.apps.travels.tracer.databinding.ActivityCoffeeCreatorBinding;
import cs10.apps.travels.tracer.databinding.ContentCoffeeCreatorBinding;
import cs10.apps.travels.tracer.pages.coffee.db.CoffeeDao;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Coffee;

public class CoffeeCreator extends FormActivity {
    private ContentCoffeeCreatorBinding content;

    private final String[] messages = {
            "Café creado con éxito",
            "Por favor complete los campos para continuar",
            "Formato de hora incorrecto",
            "Formato de fecha incorrecto",
            "Error general de formato"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCoffeeCreatorBinding binding = ActivityCoffeeCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        content = binding.contentCoffeeCreator;
        binding.toolbarLayout.setTitle("Registrar Café");

        binding.fab.setOnClickListener(view -> performDone());

        autoComplete();
        Utils.loadCoffeeBanner(binding.appbarImage);
    }

    private void autoComplete(){
        // set today values
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        content.etDate.setText(Utils.dateFormat(day, month, year));
        content.etHour.setText(Utils.hourFormat(hour, minute));

        // search last price
        doInBackground(() -> {
            CoffeeDao dao = MiDB.getInstance(this).coffeeDao();
            Coffee last = dao.getLastCoffee();
            if (last == null) return;

            double price = last.getPrice();

            doInForeground(() -> {
                content.etPrice.setText(Utils.priceFormat(price));
                content.lastCoffeeInfo.setText(getString(R.string.last_coffee_info, last.getDay(), last.getMonth()));
            });
        });
    }

    private void performDone(){
        Coffee coffee = new Coffee();
        int result = onCheckEntries(coffee);

        if (result == 0) doInBackground(() -> {
            CoffeeDao dao = MiDB.getInstance(this).coffeeDao();
            dao.insert(coffee);
            doInForeground(this::finish);
        });

        Toast.makeText(getApplicationContext(), messages[result], Toast.LENGTH_LONG).show();
    }

    private int onCheckEntries(@NonNull Coffee coffee){
        String date = content.etDate.getText().toString();
        String startHour = content.etHour.getText().toString();
        String price = content.etPrice.getText().toString().replace("$","");

        if (date.isEmpty() || startHour.isEmpty() || price.isEmpty()) return 1;

        String[] hourParams = startHour.split(":");
        if (hourParams.length != 2){
            content.etHour.setError("Ingrese una hora válida");
            return 2;
        }

        String[] dateParams = date.split("/");
        if (dateParams.length != 3){
            content.etDate.setError("Ingrese una fecha válida");
            return 3;
        }

        try {
            coffee.setHour(Integer.parseInt(hourParams[0]));
            coffee.setMinute(Integer.parseInt(hourParams[1]));
            coffee.setDay(Integer.parseInt(dateParams[0]));
            coffee.setMonth(Integer.parseInt(dateParams[1]));
            coffee.setYear(Integer.parseInt(dateParams[2]));
            coffee.setPrice(Double.parseDouble(price));
        } catch (Exception e){
            e.printStackTrace();
            return 4;
        }

        return 0;
    }

}