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

import com.google.android.material.tabs.TabLayout;

import java.util.List;

import cs10.apps.common.android.ui.CS_Fragment;
import cs10.apps.travels.tracer.adapter.TravelAdapter;
import cs10.apps.travels.tracer.databinding.FragmentTravelsBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ViajesDao;
import cs10.apps.travels.tracer.enums.TransportType;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.joins.ColoredTravel;
import cs10.apps.travels.tracer.modules.AutoRater;
import cs10.apps.travels.tracer.viewmodel.RootVM;

public class MyTravelsFragment extends CS_Fragment {
    private FragmentTravelsBinding binding;
    private TravelAdapter adapter;

    private RootVM rootVM;
    private boolean filterAvailable = false;

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

        // View Model
        rootVM.getLoading().observe(getViewLifecycleOwner(), it -> {
            if (it) showLoading();
            else showContent();
        });

        RecyclerView rv = binding.recycler;
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // Filter
        binding.roundedTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String constraint;

                switch (tab.getPosition()){
                    case 1:
                        constraint = TransportType.BUS.toString();
                        break;
                    case 2:
                        constraint = TransportType.TRAIN.toString();
                        break;
                    default:
                        constraint = null;
                        break;
                }

                adapter.getFilter().filter(constraint);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void showContent() {
        binding.recycler.setVisibility(View.VISIBLE);
        binding.viewLoading.setVisibility(View.GONE);

        binding.tabsBox.setVisibility(filterAvailable ? View.VISIBLE : View.GONE);
    }

    private void showLoading() {
        binding.recycler.setVisibility(View.GONE);
        binding.viewLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        showLoading();
        filterAvailable = false;

        // rootVM.enableLoading();
        doInBackground(this::buildData);
        binding.roundedTabs.selectTab(binding.roundedTabs.getTabAt(0));
    }

    private void buildData() {
        ViajesDao dao = MiDB.getInstance(getContext()).viajesDao();
        List<ColoredTravel> viajes = null;

        // activity intent extras
        if (getActivity() != null){
            Intent i = getActivity().getIntent();
            int line = i.getIntExtra("number", -1);
            String ramal = i.getStringExtra("ramal");
            String dest = i.getStringExtra("dest");
            int wd = i.getIntExtra("wd", -1);

            if (line != -1) {
                if (wd != -1) viajes = dao.getAllTravelsOn(line, wd);
                else if (dest != null) viajes = dao.getAllToDestination(line, dest);
                else if (ramal == null) viajes = dao.getAllFromNoRamal(line);
                else viajes = dao.getAllFromRamal(line, ramal);
            }
        }

        if (viajes == null) {
            viajes = dao.getAllPlusColors();
            filterAvailable = true;
        }

        // Oct 15: calculate rate based on duration
        AutoRater.Companion.calculateRate(viajes, dao);

        int ogSize = adapter.getItemCount();
        int newSize = viajes.size();
        adapter.updateList(viajes);

        //doInForeground(() -> adapter.getFilter().filter(null));

        if (ogSize == 0) doInForeground(() -> adapter.notifyItemRangeInserted(0, newSize));
        else doInForeground(adapter::notifyDataSetChanged);

        doInForeground(this::showContent);
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
        if (viaje.getTipo() > 1) return;    // disable car travels edition

        Intent intent = new Intent(getActivity(), viaje.getTipo() == 0 ? BusTravelEditor.class : TrainTravelEditor.class);
        intent.putExtra("travelId", viaje.getId());
        startActivity(intent);
    }
}