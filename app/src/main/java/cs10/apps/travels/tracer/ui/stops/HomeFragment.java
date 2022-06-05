package cs10.apps.travels.tracer.ui.stops;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.DrawerActivity;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.FragmentHomeBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Parada;

public class HomeFragment extends CS_Fragment {
    private FragmentHomeBinding binding;
    private HomeSliderAdapter sliderAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sliderAdapter = new HomeSliderAdapter(this);
        binding.viewPager.setAdapter(sliderAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.pbar.setVisibility(View.VISIBLE);

        if (getActivity() instanceof DrawerActivity){
            DrawerActivity activity = (DrawerActivity) getActivity();
            activity.requestCurrentLocation().addOnSuccessListener(location -> {
                if (location != null) doInBackground(() -> onBuildHome(location));
            });
        }
    }

    public void onBuildHome(Location location){
        if (getActivity() instanceof DatabaseCallback){
            DatabaseCallback callback = (DatabaseCallback) getActivity();
            MiDB miDB = callback.getInstanceWhenFinished();

            List<Parada> favourites = miDB.paradasDao().getFavouritesAndTrains();
            Utils.orderByProximity(favourites, location.getLatitude(), location.getLongitude());
            if (favourites.isEmpty()) return;

            double maxDistance = favourites.get(favourites.size()-1).getDistance();
            double relativeDistance = 1 - (favourites.get(0).getDistance() / maxDistance);

            doInForeground(() -> {
                binding.pbar.setVisibility(View.GONE);
                sliderAdapter.setFavourites(favourites);
                sliderAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), Math.round(relativeDistance*100) + "% de cercanía", Toast.LENGTH_LONG).show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class HomeSliderAdapter extends FragmentStateAdapter {
        private List<Parada> favourites;

        public HomeSliderAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        public void setFavourites(List<Parada> favourites) {
            this.favourites = favourites;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            StopArrivalsFragment fragment = new StopArrivalsFragment();
            fragment.setStopName(favourites.get(position).getNombre());
            return fragment;
        }

        @Override
        public int getItemCount() {
            return favourites == null ? 0 : favourites.size();
        }
    }
}