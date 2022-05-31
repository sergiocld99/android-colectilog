package cs10.apps.travels.tracer.ui.travels;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.adapter.EditTravelCallback;
import cs10.apps.travels.tracer.adapter.TravelsAdapter;
import cs10.apps.travels.tracer.databinding.FragmentTrainsBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ViajesDao;
import cs10.apps.travels.tracer.model.Viaje;

public class TravelsFragment extends CS_Fragment implements EditTravelCallback {
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

        doInBackground(() -> {
            ViajesDao dao = MiDB.getInstance(getContext()).viajesDao();
            List<Viaje> viajes = dao.getAll();
            adapter.setViajes(viajes);
            doInForeground(adapter::notifyDataSetChanged);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDeleteTravel(long travelId, int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(adapter.getViajes().get(pos).getStartAndEnd());
        builder.setMessage("¿Quieres eliminar este viaje de tu historial?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> doInBackground(() -> {
            ViajesDao dao = MiDB.getInstance(getContext()).viajesDao();
            dao.delete(travelId);
            doInForeground(() -> adapter.notifyItemRemoved(pos));
        }));

        builder.setNeutralButton("Volver", (dialogInterface, i) -> dialogInterface.cancel());
        builder.create().show();
    }

    @Override
    public void onEditTravel(long id) {
        Intent intent = new Intent(getActivity(), TravelEditor.class);
        intent.putExtra("travelId", id);
        startActivity(intent);
    }
}