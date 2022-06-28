package cs10.apps.travels.tracer.ui.stops;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.LinkedList;
import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.DrawerActivity;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.adapter.LocatedStopAdapter;
import cs10.apps.travels.tracer.databinding.FragmentStopsBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Parada;

public class StopListFragment extends CS_Fragment {
    private FragmentStopsBinding binding;
    private LocatedStopAdapter adapter;
    private MiDB miDB;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStopsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new LocatedStopAdapter(new LinkedList<>(), (parada) -> {
            onEditStop(parada.getNombre());
            return null;
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof DrawerActivity){
            DrawerActivity activity = (DrawerActivity) getActivity();
            Double latitude = activity.getLatitude();
            Double longitude = activity.getLongitude();

            if (latitude != null && latitude != 0) doInBackground(() -> {
                miDB = MiDB.getInstance(getContext());
                List<Parada> paradas = miDB.paradasDao().getAll();
                Utils.orderByProximity(paradas, latitude, longitude);

                int originalSize = adapter.getItemCount();
                adapter.setParadasList(paradas);

                if (originalSize == 0) doInForeground(() ->
                        adapter.notifyItemRangeInserted(0, paradas.size()));
                else doInForeground(adapter::notifyDataSetChanged);
            });
        }
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