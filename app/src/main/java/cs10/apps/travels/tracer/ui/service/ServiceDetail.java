package cs10.apps.travels.tracer.ui.service;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import cs10.apps.travels.tracer.adapter.ServiceAdapter;
import cs10.apps.travels.tracer.databinding.ActivityServiceDetailBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ServicioDao;
import cs10.apps.travels.tracer.model.roca.HorarioTren;

public class ServiceDetail extends AppCompatActivity {
    private long id;
    private ServiceAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityServiceDetailBinding binding = ActivityServiceDetailBinding.inflate(getLayoutInflater());

        adapter = new ServiceAdapter();
        adapter.setContext(this);

        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        id = getIntent().getLongExtra("id", 0);

        new Thread(() -> {
            ServicioDao dao = MiDB.getInstance(this).servicioDao();
            List<HorarioTren> horarios = dao.getRecorrido(id);
            adapter.setHorarios(horarios);
            runOnUiThread(adapter::notifyDataSetChanged);
        }, "serviceDetail").start();
    }
}
