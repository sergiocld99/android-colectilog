package cs10.apps.travels.tracer;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.material.navigation.NavigationView;

import cs10.apps.travels.tracer.data.generator.DelayData;
import cs10.apps.travels.tracer.data.generator.GlewFiller;
import cs10.apps.travels.tracer.data.generator.LaPlataFiller;
import cs10.apps.travels.tracer.data.generator.ViaCircuitoFiller;
import cs10.apps.travels.tracer.databinding.ActivityDrawerBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.ui.coffee.CoffeeCreator;
import cs10.apps.travels.tracer.ui.stops.DatabaseCallback;
import cs10.apps.travels.tracer.ui.travels.TrainTravelCreator;
import cs10.apps.travels.tracer.ui.travels.BusTravelCreator;
import cs10.apps.travels.tracer.viewmodel.LocationVM;

public class DrawerActivity extends AppCompatActivity implements DatabaseCallback {
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDrawerBinding binding;
    private FusedLocationProviderClient client;

    private Thread dbThread;
    private LocationCallback locationCallback;

    // ViewModel
    private LocationVM locationVM;

    // Results
    ActivityResultLauncher<Object> fabLauncher = registerForActivityResult(new ActivityResultContract<Object, Intent>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Object input) {
            return new Intent(context, SelectTravelType.class);
        }

        @Override
        public Intent parseResult(int resultCode, @Nullable Intent intent) {
            switch (resultCode){
                case 0: return new Intent(DrawerActivity.this, BusTravelCreator.class);
                case 1: return new Intent(DrawerActivity.this, TrainTravelCreator.class);
                default: return null;
            }
        }
    }, result -> {
        if (result != null) startActivity(result);
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarDrawer.toolbar);

        // FAB
        binding.appBarDrawer.fab.setOnClickListener(view -> {
            // Intent intent = new Intent(DrawerActivity.this, SelectTravelType.class);
            // startActivity(intent);

            fabLauncher.launch(null);
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_colectivos,
                R.id.nav_trenes, R.id.nav_proximos, R.id.nav_prox_destinos, R.id.nav_paradas)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_drawer);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        client = getFusedLocationProviderClient(this);
        Utils.checkPermissions(this);

        // ViewModel
        locationVM = new ViewModelProvider(this).get(LocationVM.class);

        // Re-create Via Circuito if is needed
        dbThread = new Thread(() -> {
            MiDB db = MiDB.getInstance(this);

            // actualización 1: vias temperley y quilmes
            int count = db.servicioDao().getServicesCount("Temperley");
            if (count == 0) {
                db.servicioDao().dropHorarios();        // first this
                db.servicioDao().dropServicios();       // then this

                DelayData delayData = new DelayData();
                ViaCircuitoFiller filler = new ViaCircuitoFiller(delayData);
                filler.create2_Q(db);       // hoja 2 de la planilla
                filler.create2_T(db);       // hoja 1 de la planilla
                runOnUiThread(() -> Toast.makeText(this, "Vias Temperley y Bosques creados con éxito", Toast.LENGTH_LONG).show());
            }

            // actualización 2: servicio la plata
            count = db.servicioDao().getServicesCount("La Plata");
            if (count == 0) {
                DelayData delayData = new DelayData();
                LaPlataFiller filler = new LaPlataFiller(delayData);
                filler.createIda(db);
                filler.createVuelta(db);
                runOnUiThread(() -> Toast.makeText(this, "Ramal La Plata creado con éxito", Toast.LENGTH_LONG).show());
            }

            // actualizacion 3: servicio glew / korn
            count = db.servicioDao().getServicesCount("Glew");
            if (count == 0) {
                DelayData delayData = new DelayData();
                GlewFiller filler = new GlewFiller(delayData);
                db.servicioDao().deleteHorariosSince(2244);
                db.servicioDao().deleteServicesSince(2244);
                filler.createIda(db);
                filler.createVuelta(db);
                runOnUiThread(() -> Toast.makeText(this, "Ramal Glew/Korn creado con éxito", Toast.LENGTH_LONG).show());
            }

            // actualización 4: dias de la semana
            for (Viaje v : db.viajesDao().getUndefinedWeekDays()) {
                Utils.setWeekDay(v);
                db.viajesDao().update(v);
            }

        }, "dbUpdater");

        dbThread.start();

        // Location
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                locationVM.getLocation().postValue(locationResult.getLastLocation());
            }
        };

        // Keep device awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_coffee) {
            startActivity(new Intent(this, CoffeeCreator.class));
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.removeLocationUpdates(locationCallback);
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() throws SecurityException {
        if (!Utils.checkPermissions(this)) return;

        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(10000);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        client.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public MiDB getInstanceWhenFinished() {
        try {
            if (dbThread != null) dbThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return MiDB.getInstance(this);
    }
}