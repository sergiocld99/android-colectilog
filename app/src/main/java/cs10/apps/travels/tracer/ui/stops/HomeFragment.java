package cs10.apps.travels.tracer.ui.stops;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Calendar;
import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.FragmentHomeBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Parada;
import cs10.apps.travels.tracer.viewmodel.HomeVM;
import cs10.apps.travels.tracer.viewmodel.LocationVM;

public class HomeFragment extends CS_Fragment {
    private FragmentHomeBinding binding;
    private HomeSliderAdapter sliderAdapter;

    // ViewModel
    private HomeVM homeVM;
    private LocationVM locationVM;
    private Observer<Location> firstLocationObserver;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sliderAdapter = new HomeSliderAdapter(this);
        binding.viewPager.setAdapter(sliderAdapter);

        homeVM = new ViewModelProvider(requireActivity()).get(HomeVM.class);
        locationVM = new ViewModelProvider(requireActivity()).get(LocationVM.class);

        homeVM.getFavoriteStops().observe(getViewLifecycleOwner(), favoriteStops -> {
            sliderAdapter.setFavourites(favoriteStops);
            sliderAdapter.notifyDataSetChanged();
            binding.pbar.setVisibility(View.GONE);
        });

        firstLocationObserver = location -> {
            binding.pbar.setVisibility(View.VISIBLE);
            onBuildHome(location);
        };

        locationVM.getLocation().observe(getViewLifecycleOwner(), firstLocationObserver);
    }

    public void onBuildHome(Location location){
        // avoid multiple calls
        locationVM.getLocation().removeObserver(firstLocationObserver);

        if (getActivity() instanceof DatabaseCallback) doInBackground(() -> {
            DatabaseCallback callback = (DatabaseCallback) getActivity();
            MiDB miDB = callback.getInstanceWhenFinished();

            // this month favorites
            Calendar calendar = Calendar.getInstance();
            int currentWeekDay = calendar.get(Calendar.DAY_OF_WEEK);

            List<Parada> favoriteStops = miDB.paradasDao().getFavouriteStops(currentWeekDay);
            Utils.orderByProximity(favoriteStops, location.getLatitude(), location.getLongitude());

            homeVM.getFavoriteStops().postValue(favoriteStops);

            double maxDistance = favoriteStops.get(favoriteStops.size()-1).getDistance();
            homeVM.getMaxDistance().postValue(maxDistance);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class HomeSliderAdapter extends FragmentStateAdapter {
        private List<Parada> favourites;

        public HomeSliderAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        public void setFavourites(List<Parada> favourites) {
            this.favourites = favourites;
        }

        @NonNull @Override
        public Fragment createFragment(int position) {
            StopArrivalsFragment fragment = new StopArrivalsFragment();

            Bundle args = new Bundle();
            args.putInt("pos", position);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getItemCount() {
            return favourites == null ? 0 : favourites.size();
        }
    }
}