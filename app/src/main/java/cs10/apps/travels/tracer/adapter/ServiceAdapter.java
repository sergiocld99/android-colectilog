package cs10.apps.travels.tracer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
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
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setHorarios(List<HorarioTren> horarios) {
        this.horarios = horarios;
    }

    @NonNull @Override
    public ServiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemServiceBinding binding = ItemServiceBinding.inflate(inflater, parent, false);
        return new ServiceHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceHolder holder, int position) {
        HorarioTren item = horarios.get(position);
        holder.binding.tvStation.setText(item.getStation());
        holder.binding.tvArrivalTime.setText(Utils.hourFormat(item.getHour(), item.getMinute()));

        if (position == 0 || position == getItemCount()-1){
            holder.binding.getRoot().setBackgroundColor(context.getResources().getColor(R.color.purple_700));
        } else if (item.getService() == 0) holder.binding.getRoot().setBackground(null);
        else holder.binding.getRoot().setBackgroundColor(context.getResources().getColor(R.color.bus));
    }

    @Override
    public int getItemCount() {
        return horarios == null ? 0 : horarios.size();
    }

    protected static class ServiceHolder extends RecyclerView.ViewHolder {
        protected ItemServiceBinding binding;

        public ServiceHolder(@NonNull ItemServiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
