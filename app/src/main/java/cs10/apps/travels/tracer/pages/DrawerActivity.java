package cs10.apps.travels.tracer.pages;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
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

import cs10.apps.common.android.ui.CSActivity;
import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.common.constants.ResultCodes;
import cs10.apps.travels.tracer.data.generator.DelayData;
import cs10.apps.travels.tracer.data.generator.GlewFiller;
import cs10.apps.travels.tracer.data.generator.LaPlataFiller;
import cs10.apps.travels.tracer.data.generator.UniversitarioFiller;
import cs10.apps.travels.tracer.data.generator.ViaCircuitoFiller;
import cs10.apps.travels.tracer.databinding.ActivityDrawerBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.common.enums.SelectOption;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.pages.manage_zones.ZoneCreator;
import cs10.apps.travels.tracer.pages.registry.creator.BusTravelCreator;
import cs10.apps.travels.tracer.pages.registry.creator.CarTravelCreator;
import cs10.apps.travels.tracer.pages.registry.creator.MetroTravelCreator;
import cs10.apps.travels.tracer.pages.registry.creator.TrainTravelCreator;
import cs10.apps.travels.tracer.pages.stops.creator.StopCreator;
import cs10.apps.travels.tracer.pages.coffee.CoffeeCreator;
import cs10.apps.travels.tracer.utils.Utils;
import cs10.apps.travels.tracer.viewmodel.LocationVM;
import cs10.apps.travels.tracer.viewmodel.RootVM;

public class DrawerActivity extends CSActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDrawerBinding binding;
    private FusedLocationProviderClient client;

    private Thread dbThread;
    private LocationCallback locationCallback;

    private LocationVM locationVM;
    private RootVM rootVM;

    // UI
    private NavController navController;

    // Results
    ActivityResultLauncher<Object> fabLauncher = registerForActivityResult(new ActivityResultContract<Object, Intent>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Object input) {
            return new Intent(context, SelectTravelType.class);
        }

        @Override
        public Intent parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode < 0 || resultCode >= SelectOption.values().length) return null;
            SelectOption opSelected = SelectOption.values()[resultCode];

            switch (opSelected){
                case BUS_TRAVEL: return new Intent(DrawerActivity.this, BusTravelCreator.class);
                case TRAIN_TRAVEL: return new Intent(DrawerActivity.this, TrainTravelCreator.class);
                case CAR_TRAVEL: return new Intent(DrawerActivity.this, CarTravelCreator.class);
                case METRO_TRAVEL: return new Intent(DrawerActivity.this, MetroTravelCreator.class);
                case STOP: return new Intent(DrawerActivity.this, StopCreator.class);
                case ZONE: return new Intent(DrawerActivity.this, ZoneCreator.class);
                default: return null;
            }
        }
    }, result -> {
        if (result != null) startActivityForResult(result, ResultCodes.CREATION_REQUEST);
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarDrawer.toolbar);

        // View Model
        rootVM = new ViewModelProvider(this).get(RootVM.class);
        rootVM.getLoading().observe(this, value -> {
            if (value) binding.appBarDrawer.pbar.setVisibility(View.VISIBLE);
            else binding.appBarDrawer.pbar.setVisibility(View.GONE);
        });

        // FAB
        binding.appBarDrawer.fab.setOnClickListener(view -> fabLauncher.launch(null));

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_live, R.id.nav_colectivos,
                R.id.nav_lines, R.id.nav_zones, R.id.nav_trenes,
                R.id.nav_paths, R.id.nav_paradas)
                .setOpenableLayout(binding.drawerLayout)
                .build();

        this.navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_drawer);

        // setup drawer layout
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        NavOptions options = new NavOptions.Builder()
                .setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
                .setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
                .setPopEnterAnim(androidx.navigation.ui.R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(androidx.navigation.ui.R.anim.nav_default_pop_exit_anim)
                .build();

        client = getFusedLocationProviderClient(this);
        Utils.checkPermissions(this);

        // ViewModel
        locationVM = new ViewModelProvider(this).get(LocationVM.class);

        // Create services if it's needed
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
                doInForeground(() -> showLongToast("Vias Temperley y Bosques creados con éxito"));
            }

            // actualización 2: servicio la plata
            count = db.servicioDao().getServicesCount("La Plata");
            if (count == 0) {
                DelayData delayData = new DelayData();
                LaPlataFiller filler = new LaPlataFiller(delayData);
                filler.createIda(db);
                filler.createVuelta(db);
                doInForeground(() -> showLongToast("Ramal La Plata creado con éxito"));
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
                doInForeground(() -> showLongToast("Ramal Glew/Korn creado con éxito"));
            }

            // actualización 4: dias de la semana
            for (Viaje v : db.viajesDao().getUndefinedWeekDays()) {
                Utils.setWeekDay(v);
                db.viajesDao().update(v);
            }

            // actualización 5: tren universitario
            if (db.servicioDao().getServicesCount("La Plata (Universitario)") == 0){
                db.servicioDao().deleteHorariosSince(2452);     // primero los hs
                db.servicioDao().deleteServicesSince(2452);         // luego los serv
                DelayData delayData = new DelayData();
                UniversitarioFiller filler = new UniversitarioFiller(delayData);
                filler.createIda(db);
                filler.createVuelta(db);
                doInForeground(() -> showLongToast("Tren Universitario creado con éxito"));
            }

        }, "dbUpdater");
        dbThread.start();

        // Location
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                locationVM.updateCurrentLocation(locationResult.getLastLocation());
            }
        };

        // Keep device awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // process intent
        Intent intent = getIntent();
        if (intent.getBooleanExtra("openLive", false)){
            switchToLiveFragment();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ResultCodes.CREATION_REQUEST){
            if (resultCode == ResultCodes.OPEN_LIVE_FRAGMENT) {
                switchToLiveFragment();
            }
        }
    }

    private void switchToLiveFragment(){
        showShortToast("Abriendo sección \"En vivo\"");

        // Home item is selectable using the following sentence instead of navController.navigate()
        binding.navView.getMenu().performIdentifierAction(R.id.nav_live, 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
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
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
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

}