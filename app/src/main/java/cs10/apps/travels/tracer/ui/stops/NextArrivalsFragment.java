package cs10.apps.travels.tracer.ui.stops;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import cs10.apps.common.android.ui.CS_Fragment;
import cs10.apps.travels.tracer.adapter.NextArrivalAdapter;
import cs10.apps.travels.tracer.databinding.FragmentStopsBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.ScheduledParada;

public class NextArrivalsFragment extends CS_Fragment {
    private FragmentStopsBinding binding;
    private NextArrivalAdapter adapter;
    private MiDB miDB;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStopsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new NextArrivalAdapter(new LinkedList<>(), scheduledParada -> {
            onEditStop(scheduledParada.getNombre());
            return null;
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        doInBackground(() -> {
            Calendar c = Calendar.getInstance();
            int h = c.get(Calendar.HOUR_OF_DAY);
            int m = c.get(Calendar.MINUTE);
            miDB = MiDB.getInstance(getContext());
            List<ScheduledParada> paradas = miDB.paradasDao().getScheduledStopsFrom(h, m);
            int originalSize = adapter.getItemCount();
            adapter.setList(paradas);

            if (originalSize == 0) doInForeground(() -> adapter.notifyItemRangeInserted(0, paradas.size()));
            else doInForeground(adapter::notifyDataSetChanged);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onEditStop(String stopName) {
        Intent intent = new Intent(getActivity(), StopEditor.class);
        intent.putExtra("stopName", stopName);
        startActivity(intent);
    }
}