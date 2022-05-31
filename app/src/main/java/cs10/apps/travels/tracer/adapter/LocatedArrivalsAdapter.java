package cs10.apps.travels.tracer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.ItemStopBinding;
import cs10.apps.travels.tracer.model.Viaje;

public class LocatedArrivalsAdapter extends RecyclerView.Adapter<LocatedArrivalsAdapter.StopViewHolder> {
    private List<Viaje> viajes;
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setViajes(List<Viaje> viajes) {
        this.viajes = viajes;
    }

    @NonNull @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemStopBinding binding = ItemStopBinding.inflate(inflater, parent, false);
        return new StopViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        Viaje item = viajes.get(position);
        holder.binding.tvName.setText(item.getLineInformation());
        Drawable icon, bg;

        if (item.getTipo() == 0) {
            icon = AppCompatResources.getDrawable(context, R.drawable.ic_bus);
        } else {
            icon = AppCompatResources.getDrawable(context, R.drawable.ic_train);
        }

        bg = AppCompatResources.getDrawable(context, Utils.colorFor(item.getLinea()));
        holder.binding.ivType.setImageDrawable(icon);
        holder.binding.getRoot().setBackground(bg);

        holder.binding.tvLocation.setText("Hora " + Utils.hourFormat(item.getStartHour(), item.getStartMinute()));
        holder.binding.tvLocation.setVisibility(View.VISIBLE);
        holder.binding.tvSwitcher.setVisibility(View.GONE);

        // always
        holder.binding.tvStartCount.setText(context.getString(R.string.destination, item.getNombrePdaFin()));
    }

    @Override
    public int getItemCount() {
        return viajes == null ? 0 : viajes.size();
    }

    protected class StopViewHolder extends RecyclerView.ViewHolder {
        protected ItemStopBinding binding;

        public StopViewHolder(@NonNull ItemStopBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
