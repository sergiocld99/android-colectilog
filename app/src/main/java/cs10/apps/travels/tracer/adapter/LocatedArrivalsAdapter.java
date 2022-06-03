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
import cs10.apps.travels.tracer.db.filler.Station;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.roca.ArriboTren;
import cs10.apps.travels.tracer.ui.stops.DepartCallback;
import cs10.apps.travels.tracer.ui.stops.ETA_Switcher;

public class LocatedArrivalsAdapter extends RecyclerView.Adapter<LocatedArrivalsAdapter.StopViewHolder> implements DepartCallback {
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
        Drawable icon, bg;

        if (item instanceof ArriboTren){
            ArriboTren arribo = (ArriboTren) item;
            boolean temperley = arribo.isFutureStation(Station.TEMPERLEY);
            boolean quilmes = arribo.isFutureStation(Station.QUILMES);

            if (temperley && !quilmes) holder.binding.tvName.setText("Via Temperley");
            else if (quilmes && !temperley) holder.binding.tvName.setText("Via Quilmes");
            else holder.binding.tvName.setText(item.getRamal());
        } else holder.binding.tvName.setText(item.getLineInformation());

        if (item.getEndHour() != null){
            holder.binding.tvStartCount.setText("A " + item.getNombrePdaFin() + " (" + Utils.hourFormat(item.getEndHour(), item.getEndMinute()) + ")");
        } else holder.binding.tvStartCount.setText(context.getString(R.string.destination, item.getNombrePdaFin()));

        if (item.getTipo() == 0) {
            icon = AppCompatResources.getDrawable(context, R.drawable.ic_bus);
        } else {
            icon = AppCompatResources.getDrawable(context, R.drawable.ic_train);
        }

        bg = AppCompatResources.getDrawable(context, Utils.colorFor(item.getLinea()));
        holder.binding.ivType.setImageDrawable(icon);
        holder.binding.getRoot().setBackground(bg);

        // Animación de recorrido solo para el primer item (es conflictivo usar 2 a la vez)
        if (position == 0 && item instanceof ArriboTren){
            holder.switcher.setCallback(this);
            holder.switcher.setItem((ArriboTren) item);
            holder.switcher.setTvSwitcher(holder.binding.tvSwitcher);
            holder.switcher.startAnimation();
            holder.binding.tvLocation.setVisibility(View.GONE);
            holder.binding.tvSwitcher.setVisibility(View.VISIBLE);
        } else {
            holder.switcher.stop();
            holder.binding.tvLocation.setText("Hora " + Utils.hourFormat(item.getStartHour(), item.getStartMinute()));
            holder.binding.tvLocation.setVisibility(View.VISIBLE);
            holder.binding.tvSwitcher.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return viajes == null ? 0 : viajes.size();
    }

    @Override
    public void onDepart() {
        viajes.remove(0);
        notifyItemRemoved(0);

        if (getItemCount() > 0) notifyItemChanged(0);
    }

    @Override
    public Context getContext() {
        return context;
    }

    protected class StopViewHolder extends RecyclerView.ViewHolder {
        protected ItemStopBinding binding;
        protected ETA_Switcher switcher;

        public StopViewHolder(@NonNull ItemStopBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.switcher = new ETA_Switcher();
        }
    }
}
