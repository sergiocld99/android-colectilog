package cs10.apps.travels.tracer.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;
import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.FragmentBusesBinding;
import cs10.apps.travels.tracer.databinding.ViewCircularPbWithLegendBinding;
import cs10.apps.travels.tracer.databinding.ViewLineIndicatorBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.db.ViajesDao;
import cs10.apps.travels.tracer.model.PriceSum;

public class StatsFragment extends CS_Fragment {
    public static final int VIAJE_PARA_SALDO = 64;
    public static final double SALDO_TEST = 887.47;
    private FragmentBusesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentBusesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        doInBackground(() -> {
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH) + 1;

            MiDB db = MiDB.getInstance(getContext());
            ViajesDao dao = db.viajesDao();

            double buses = dao.getTotalSpentInBuses(month);
            double trains = dao.getTotalSpentInTrains(month);
            double coffee = db.coffeeDao().getTotalSpent(month);
            double total = Math.max(buses + trains + coffee, 1);
            List<PriceSum> sums = dao.getMostExpensiveBus(month);

            double sinceBuses = dao.getSpentInBusesSince(VIAJE_PARA_SALDO);
            double sinceTrains = dao.getSpentInTrainsSince(VIAJE_PARA_SALDO);
            double charges = db.recargaDao().getTotalChargedSince(0);
            double money = SALDO_TEST - sinceBuses - sinceTrains - coffee + charges;

            doInForeground(() -> updateUI(money, buses, trains, coffee, total, sums));
        });
    }

    public void updateUI(double money, double buses, double trains, double coffee, double total, List<PriceSum> sums){
        binding.title.setText(getString(R.string.current_money, Utils.priceFormat(money)));

        int porceBuses = (int) Math.round(buses * 100 / total);
        int porceTrains = (int) Math.round(trains * 100 / total);
        int porceCoffee = (int) Math.round(coffee * 100 / total);

        binding.busPb.legendAtBottom.setText(Utils.priceFormat(buses));
        binding.trainsPb.legendAtBottom.setText(Utils.priceFormat(trains));
        binding.coffeePb.legendAtBottom.setText(Utils.priceFormat(coffee));
        binding.busPb.porcentage.setText(getString(R.string.porcentage_value, porceBuses));
        binding.trainsPb.porcentage.setText(getString(R.string.porcentage_value, porceTrains));
        binding.coffeePb.porcentage.setText(getString(R.string.porcentage_value, porceCoffee));
        binding.busPb.pb.setProgress(porceBuses);
        binding.trainsPb.pb.setProgress(porceTrains);
        binding.coffeePb.pb.setProgress(porceCoffee);

        ViewCircularPbWithLegendBinding[] busPbs = {binding.bus1Pb, binding.bus2Pb, binding.bus3Pb};
        ViewLineIndicatorBinding[] busIndicators = {binding.vli1, binding.vli2, binding.vli3};

        if (sums.size() >= busPbs.length) for (int i=0; i<busPbs.length; i++){
            PriceSum ps = sums.get(i);
            int porce = (int) Math.round(ps.getSuma() * 100 / buses);
            busIndicators[i].textLineNumber.setText(String.valueOf(ps.getLinea()));
            busIndicators[i].getRoot().setCardBackgroundColor(getResources().getColor(Utils.colorFor(ps.getLinea())));
            busPbs[i].pb.setProgress(porce);
            busPbs[i].porcentage.setText(getString(R.string.porcentage_value, porce));
            busPbs[i].legendAtBottom.setText(Utils.priceFormat(ps.getSuma()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}