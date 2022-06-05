package cs10.apps.travels.tracer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.ItemServiceBinding;
import cs10.apps.travels.tracer.model.roca.HorarioTren;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceHolder> {
    private List<HorarioTren> horarios;
    private ServiceCallback callback;

    public void setCallback(ServiceCallback callback) {
        this.callback = callback;
    }

    public void setHorarios(List<HorarioTren> horarios) {
        this.horarios = horarios;
    }

    @NonNull @Override
    public ServiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(callback.getContext());
        ItemServiceBinding binding = ItemServiceBinding.inflate(inflater, parent, false);
        return new ServiceHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceHolder holder, int position) {
        HorarioTren item = horarios.get(position);
        holder.binding.tvStation.setText(item.getStation());
        holder.binding.tvArrivalTime.setText(Utils.hourFormat(item.getHour(), item.getMinute()));

        if (position == 0 || position == getItemCount()-1){
            holder.binding.getRoot().setBackgroundColor(callback.getContext().getResources().getColor(R.color.purple_700));
        } else if (item.getService() == 0) holder.binding.getRoot().setBackground(null);
        else holder.binding.getRoot().setBackgroundColor(callback.getContext().getResources().getColor(R.color.bus));

        if (item.getCombination() == null) holder.binding.tvCombination.setVisibility(View.GONE);
        else {
            HorarioTren comb = item.getCombination();
            holder.binding.tvCombination.setVisibility(View.VISIBLE);
            holder.binding.tvCombination.setText(callback.getContext().getString(R.string.combination_info, item.getCombinationRamal(), Utils.hourFormat(comb.getHour(), comb.getMinute())));
            holder.binding.getRoot().setBackgroundColor(callback.getContext().getResources().getColor(R.color.bus_324));
        }
    }

    @Override
    public int getItemCount() {
        return horarios == null ? 0 : horarios.size();
    }

    protected class ServiceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ItemServiceBinding binding;

        public ServiceHolder(@NonNull ItemServiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            HorarioTren item = horarios.get(getAdapterPosition());
            if (item.getCombination() != null){
                callback.onServiceSelected(item.getCombination().getService(), item.getCombinationRamal());
            }
        }
    }
}
