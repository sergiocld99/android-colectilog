package cs10.apps.travels.tracer.ui.stops;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Calendar;
import java.util.List;

import cs10.apps.travels.tracer.adapter.EditStopCallback;
import cs10.apps.travels.tracer.adapter.StopsToAdapter;
import cs10.apps.travels.tracer.databinding.FragmentStopsBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.ScheduledParada;

public class NextDestinationsFragment extends Fragment implements EditStopCallback {
    private FragmentStopsBinding binding;
    private StopsToAdapter adapter;
    private MiDB miDB;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStopsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new StopsToAdapter();
        adapter.setCallback(this);

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(() -> {
            Calendar c = Calendar.getInstance();
            int h = c.get(Calendar.HOUR_OF_DAY);
            int m = c.get(Calendar.MINUTE);
            miDB = MiDB.getInstance(getContext());
            List<ScheduledParada> paradas = miDB.paradasDao().getScheduledStopsTo(h, m);
            int originalSize = adapter.getItemCount();
            adapter.setParadas(paradas);

            if (getActivity() != null){
                if (originalSize == 0) getActivity().runOnUiThread(() ->
                        adapter.notifyItemRangeInserted(0, paradas.size()));
                else getActivity().runOnUiThread(adapter::notifyDataSetChanged);
            }
        }, "fillStopsRecycler").start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onEditStop(String stopName) {
        Intent intent = new Intent(getActivity(), StopEditor.class);
        intent.putExtra("stopName", stopName);
        startActivity(intent);
    }
}