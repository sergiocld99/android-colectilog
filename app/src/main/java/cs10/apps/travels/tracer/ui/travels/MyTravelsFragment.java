package cs10.apps.travels.tracer.ui.travels;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.adapter.TravelAdapter;
import cs10.apps.travels.tracer.databinding.FragmentTravelsBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ViajesDao;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.viewmodel.RootVM;

public class MyTravelsFragment extends CS_Fragment {
    private FragmentTravelsBinding binding;
    private TravelAdapter adapter;

    private RootVM rootVM;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTravelsBinding.inflate(inflater, container, false);
        rootVM = new ViewModelProvider(requireActivity()).get(RootVM.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new TravelAdapter(viaje -> {
            onEditTravel(viaje);
            return null;
        }, (viaje, pos) -> {
            onDeleteTravel(viaje, pos);
            return null;
        });

        // adapter.setCallback(this);

        RecyclerView rv = binding.recycler;
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        doInBackground(() -> {
            rootVM.enableLoading();
            ViajesDao dao = MiDB.getInstance(getContext()).viajesDao();
            List<Viaje> viajes = dao.getAll();
            adapter.setList(viajes);
            doInForeground(adapter::notifyDataSetChanged);
            rootVM.disableLoading();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onDeleteTravel(@NonNull Viaje viaje, int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(viaje.getStartAndEnd());
        builder.setMessage("¿Quieres eliminar este viaje de tu historial?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> doInBackground(() -> {
            ViajesDao dao = MiDB.getInstance(getContext()).viajesDao();
            dao.delete(viaje.getId());
            doInForeground(() -> adapter.remove(pos));
        }));

        builder.setNeutralButton("Volver", (dialogInterface, i) -> dialogInterface.cancel());
        builder.create().show();
    }

    public void onEditTravel(Viaje viaje) {
        Intent intent = new Intent(getActivity(), viaje.getTipo() == 0 ? BusTravelEditor.class : TrainTravelEditor.class);
        intent.putExtra("travelId", viaje.getId());
        startActivity(intent);
    }
}