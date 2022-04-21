package cs10.apps.travels.tracer.ui.trains;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cs10.apps.travels.tracer.adapter.EditTravelCallback;
import cs10.apps.travels.tracer.adapter.TravelsAdapter;
import cs10.apps.travels.tracer.databinding.FragmentTrainsBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ViajesDao;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.ui.travels.TravelEditor;

public class TrainsFragment extends Fragment implements EditTravelCallback {
    private FragmentTrainsBinding binding;
    private TravelsAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTrainsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new TravelsAdapter();
        adapter.setCallback(this);

        RecyclerView rv = binding.recycler;
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() == null) return;

        new Thread(() -> {
            ViajesDao dao = MiDB.getInstance(getContext()).viajesDao();
            List<Viaje> viajes = dao.getAll();
            adapter.setViajes(viajes);
            getActivity().runOnUiThread(adapter::notifyDataSetChanged);
        }, "travelsFiller").start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDeleteTravel(long travelId, int pos) {
        if (getActivity() == null) return;

        new Thread(() -> {
            ViajesDao dao = MiDB.getInstance(getContext()).viajesDao();
            dao.delete(travelId);
            getActivity().runOnUiThread(() -> adapter.notifyItemRemoved(pos));
        }, "onDeleteTravel").start();
    }

    @Override
    public void onEditTravel(long id) {
        Intent intent = new Intent(getActivity(), TravelEditor.class);
        intent.putExtra("travelId", id);
        startActivity(intent);
    }
}