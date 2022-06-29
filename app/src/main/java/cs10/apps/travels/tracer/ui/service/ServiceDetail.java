package cs10.apps.travels.tracer.ui.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import cs10.apps.common.android.Clock;
import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.adapter.TrainScheduleAdapter;
import cs10.apps.travels.tracer.databinding.ActivityServiceDetailBinding;
import cs10.apps.travels.tracer.db.DynamicQuery;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ServicioDao;
import cs10.apps.travels.tracer.generator.Ramal;
import cs10.apps.travels.tracer.generator.Station;
import cs10.apps.travels.tracer.generator.TarifaData;
import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.viewmodel.ServiceVM;

public class ServiceDetail extends AppCompatActivity {
    private LinearLayoutManager llm;
    private RecyclerView.SmoothScroller scroller;
    private TrainScheduleAdapter adapter;
    private String stopName;
    private long id;

    // ViewModel
    private ActivityServiceDetailBinding binding;
    private ServiceVM serviceVM;
    private Clock clock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityServiceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // adapter
        adapter = new TrainScheduleAdapter(new LinkedList<>(), null, item -> {
            if (item.getCombination() != null)
                onServiceSelected(item.getCombination().getService(), item.getCombinationRamal());
            return null;
        });

        scroller = new LinearSmoothScroller(getApplicationContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        llm = new LinearLayoutManager(this);

        // view model
        serviceVM = new ViewModelProvider(this).get(ServiceVM.class);

        serviceVM.getService().observe(this, servicioTren -> {
            binding.tvTitle.setText(getString(R.string.service_title, servicioTren.getId(), servicioTren.getRamal()));
        });

        serviceVM.getSchedules().observe(this, horarios -> {
            int originalSize = adapter.getItemCount();
            adapter.setList(horarios);
            if (originalSize == 0) adapter.notifyItemRangeInserted(0, horarios.size());
            else adapter.notifyDataSetChanged();
            new Handler().postDelayed(() -> clock.start(), 1000);
        });

        serviceVM.getCurrent().observe(this, value -> {
            int target = (value / 3) * 3;
            scroller.setTargetPosition(target);
            adapter.setCurrent(value);
            adapter.notifyDataSetChanged();

            new Handler().postDelayed(() -> llm.startSmoothScroll(scroller), 1000);
        });

        clock = new Clock(serviceVM);

        // UI
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(llm);

        receiveExtras();
        findService();

        // Keep device awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void findService() {
        new Thread(() -> {
            TarifaData tarifaData = new TarifaData();
            ServicioDao dao = MiDB.getInstance(this).servicioDao();
            final List<HorarioTren> horarios = dao.getRecorrido(id);
            final String start = horarios.get(0).getStation();
            final String destination = horarios.get(horarios.size()-1).getStation();
            final Station current = Station.findByNombre(stopName);

            // change colour according current station
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int m = calendar.get(Calendar.MINUTE);
            int index = 0;

            scroller.setTargetPosition(0);

            for (HorarioTren h : horarios){
                if (h.getHour() == hour && h.getMinute() <= m) scroller.setTargetPosition(index);

                if (h.getStation().equals(stopName)) h.setService(1);
                else h.setService(0);

                // TARIFA
                h.setTarifa(tarifaData.getTarifa(current, h.getStation()));

                // CASO COMBINACIÓN VIA CIRCUITO > LA PLATA
                if (equals(h.getStation(), Station.BERA)){
                    if (equals(start, Station.LA_PLATA)){
                        // El servicio mostrado arrancó en La Plata -> me interesa a Bosques
                        HorarioTren comb = DynamicQuery.findCombination(this, Ramal.BOSQUES_Q_TEMPERLEY.getNombre(), h);
                        h.setCombinationRamal(Station.TEMPERLEY.getSimplified());
                        h.setCombination(comb);
                    } else if (destination.equals(start)){
                        // El servicio mostrado es un via circuito -> me interesa a La Plata
                        HorarioTren comb = DynamicQuery.findCombination(this, Station.LA_PLATA.getSimplified(), h);
                        h.setCombinationRamal(Station.LA_PLATA.getSimplified());
                        h.setCombination(comb);
                    }
                }

                // CASO COMBINACIÓN TEMPERLEY (GLEW > VIA CIRCUITO)
                for (Station s : new Station[]{Station.TEMPERLEY, Station.LOMAS}){
                    if (equals(h.getStation(), s)){
                        if (equals(start, Station.GLEW) || equals(start, Station.KORN)){
                            // El servicio mostrado empezó en Glew o Korn -> me interesa a Bosques
                            HorarioTren comb = DynamicQuery.findCombination(this, Ramal.BOSQUES_T.getNombre(), h);
                            h.setCombinationRamal(Station.BOSQUES.getSimplified());
                            h.setCombination(comb);
                        } else if (destination.equals(start) || equals(start, Station.BOSQUES)){
                            // El servicio mostrado es un via circuito -> me interesa a Korn
                            HorarioTren comb = DynamicQuery.findCombination(this, Station.KORN.getSimplified(), h);
                            h.setCombinationRamal("Korn");
                            h.setCombination(comb);
                        }
                    }
                }

                index++;
            }

            runOnUiThread(() -> serviceVM.getSchedules().postValue(horarios));
            // adapter.setList(horarios);

        }, "serviceDetail").start();
    }

    private void receiveExtras() {
        id = getIntent().getLongExtra("id", 0);
        stopName = getIntent().getStringExtra("station");
        String ramal = getIntent().getStringExtra("ramal");

        serviceVM.setData(id, ramal);
    }

    private boolean equals(String s1, Station s2){
        return s1.equals(s2.getNombre());
    }

    private void onServiceSelected(long id, String ramal) {
        Intent intent = new Intent(this, ServiceDetail.class);
        intent.putExtra("station", stopName);
        intent.putExtra("ramal", ramal);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        clock.stop();
    }
}
