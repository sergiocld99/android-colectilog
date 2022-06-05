package cs10.apps.travels.tracer;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import cs10.apps.travels.tracer.databinding.ActivityDrawerBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.generator.DelayData;
import cs10.apps.travels.tracer.generator.LaPlataFiller;
import cs10.apps.travels.tracer.generator.ViaCircuitoFiller;
import cs10.apps.travels.tracer.ui.coffee.CoffeeCreator;
import cs10.apps.travels.tracer.ui.stops.DatabaseCallback;
import cs10.apps.travels.tracer.ui.travels.TravelCreator;

public class DrawerActivity extends AppCompatActivity implements DatabaseCallback {
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDrawerBinding binding;
    private FusedLocationProviderClient client;
    private Double latitude, longitude;

    private Thread dbThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarDrawer.toolbar);

        // FAB
        binding.appBarDrawer.fab.setOnClickListener(view -> {
            Intent intent = new Intent(DrawerActivity.this, TravelCreator.class);
            startActivity(intent);
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

        client = LocationServices.getFusedLocationProviderClient(this);
        Utils.checkPermissions(this);

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
                runOnUiThread(() -> Toast.makeText(this,
                        "Vias Temperley y Bosques creados con éxito", Toast.LENGTH_LONG).show());
            }

            // actualización 2: servicio la plata
            count = db.servicioDao().getServicesCount("La Plata");
            if (count == 0){
                DelayData delayData = new DelayData();
                LaPlataFiller filler = new LaPlataFiller(delayData);
                filler.createIda(db);
                filler.createVuelta(db);
                runOnUiThread(() -> Toast.makeText(this,
                        "Ramal La Plata creado con éxito", Toast.LENGTH_LONG).show());
            }
        }, "trenesDbUpdater");

        dbThread.start();

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

        if (item.getItemId() == R.id.action_coffee){
            startActivity(new Intent(this, CoffeeCreator.class));
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentLocation();
    }

    public void getCurrentLocation() throws SecurityException {
        if (Utils.checkPermissions(this)) client.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) return;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        });
    }

    public Task<Location> requestCurrentLocation() throws SecurityException {
        if (Utils.checkPermissions(this)) return client.getLastLocation();
        return null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Override
    public MiDB getInstanceWhenFinished() {
        try {
            if (dbThread != null) dbThread.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        return MiDB.getInstance(this);
    }
}