package cs10.apps.travels.tracer.ui.service;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

import cs10.apps.travels.tracer.adapter.ServiceAdapter;
import cs10.apps.travels.tracer.databinding.ActivityServiceDetailBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ServicioDao;
import cs10.apps.travels.tracer.model.roca.HorarioTren;

public class ServiceDetail extends AppCompatActivity {
    private ActivityServiceDetailBinding binding;
    private LinearLayoutManager llm;
    private RecyclerView.SmoothScroller smoothScroller;
    private ServiceAdapter adapter;
    private String stopName;
    private int current;
    private long id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityServiceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        adapter = new ServiceAdapter();
        adapter.setContext(this);

        llm = new LinearLayoutManager(this);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(llm);

        smoothScroller = new LinearSmoothScroller(getApplicationContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        id = getIntent().getLongExtra("id", 0);
        stopName = getIntent().getStringExtra("station");
        binding.tvTitle.setText("Servicio " + id);

        new Thread(() -> {
            ServicioDao dao = MiDB.getInstance(this).servicioDao();
            final List<HorarioTren> horarios = dao.getRecorrido(id);

            // change colour according current station
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int m = calendar.get(Calendar.MINUTE);
            boolean found = false;
            int index = 0;

            smoothScroller.setTargetPosition(0);

            for (HorarioTren h : horarios){
                if (h.getHour() == hour && h.getMinute() <= m) smoothScroller.setTargetPosition(index);

                if (!found){
                    if (h.getStation().equals(stopName)){
                        h.setService(1);
                        found = true;
                    } else h.setService(0);
                } else h.setService(0);

                index++;
            }

            adapter.setHorarios(horarios);
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                new Handler().postDelayed(() -> llm.startSmoothScroll(smoothScroller), 500);
            });
        }, "serviceDetail").start();
    }
}
