package cs10.apps.travels.tracer;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import cs10.apps.travels.tracer.db.filler.ViaCircuitoFiller;
import cs10.apps.travels.tracer.ui.coffee.CoffeeCreator;
import cs10.apps.travels.tracer.ui.travels.TravelCreator;

public class DrawerActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDrawerBinding binding;
    private FusedLocationProviderClient client;
    private Double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarDrawer.toolbar);
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

        // Create Via Circuito if not exists
        new Thread(() -> {
            MiDB db = MiDB.getInstance(this);
            int count = db.servicioDao().getHorariosCount();
            if (count < 1000) {
                db.servicioDao().dropHorarios();        // first this
                db.servicioDao().dropServicios();       // then this

                ViaCircuitoFiller filler = new ViaCircuitoFiller();
                filler.create(db);
                runOnUiThread(() -> Toast.makeText(this,
                        "Via Circuito creado con éxito", Toast.LENGTH_LONG).show());
            }
        }, "viaCircuitoFiller").start();
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
}