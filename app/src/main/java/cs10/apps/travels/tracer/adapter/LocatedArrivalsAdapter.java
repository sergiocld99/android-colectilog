package cs10.apps.travels.tracer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.databinding.ItemArrivalBinding;
import cs10.apps.travels.tracer.generator.Station;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.roca.ArriboTren;
import cs10.apps.travels.tracer.ui.stops.ETA_Switcher;

public class LocatedArrivalsAdapter extends RecyclerView.Adapter<LocatedArrivalsAdapter.ArrivalViewHolder>
        implements DepartCallback {

    private List<Viaje> viajes;
    private ServiceCallback callback;

    public void setCallback(ServiceCallback callback) {
        this.callback = callback;
    }

    public void setViajes(List<Viaje> viajes) {
        this.viajes = viajes;
    }

    @NonNull @Override
    public ArrivalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(callback.getContext());
        ItemArrivalBinding binding = ItemArrivalBinding.inflate(inflater, parent, false);
        return new ArrivalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArrivalViewHolder holder, int position) {
        Viaje item = viajes.get(position);
        Drawable icon, bg;

        if (item instanceof ArriboTren){
            ArriboTren arribo = (ArriboTren) item;

            if (item.getNombrePdaFin().equals(Station.PLAZA.getNombre())){
                boolean bosques = arribo.isFutureStation(Station.BOSQUES);
                boolean temperley = arribo.isFutureStation(Station.TEMPERLEY);
                boolean quilmes = arribo.isFutureStation(Station.QUILMES);
                boolean platanos = arribo.isFutureStation(Station.PLATANOS);

                if (!bosques) {
                    if (temperley) item.setRamal("Via Temperley");
                    else if (quilmes && !platanos) item.setRamal("Via Quilmes");
                }
            }

            holder.binding.tvName.setText(item.getRamal());
        } else holder.binding.tvName.setText(item.getRamal() == null ? "" : "Ramal " + item.getRamal());

        if (item.getEndHour() != null){
            holder.binding.tvStartCount.setText("A " + item.getNombrePdaFin() + " (" + Utils.hourFormat(item.getEndHour(), item.getEndMinute()) + ")");
        } else holder.binding.tvStartCount.setText(callback.getContext().getString(R.string.destination, item.getNombrePdaFin()));

        bg = AppCompatResources.getDrawable(callback.getContext(), Utils.colorFor(item.getLinea()));

        if (item.getTipo() == 0) {
            icon = AppCompatResources.getDrawable(callback.getContext(), R.drawable.ic_bus);
        } else {
            icon = AppCompatResources.getDrawable(callback.getContext(), R.drawable.ic_train);
            if (item.getRamal() != null && item.getRamal().contains("Directo"))
                bg = AppCompatResources.getDrawable(callback.getContext(), Utils.colorFor(159));
        }

        // set confirmed icons and background color
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

        // always
        if (item.getLinea() == null) holder.binding.tvLine.setText("ROCA");
        else holder.binding.tvLine.setText(String.valueOf(item.getLinea()));
    }

    @Override
    public int getItemCount() {
        return viajes == null ? 0 : viajes.size();
    }

    @Override
    public void onDepart() {
        viajes.remove(0);
        notifyItemRemoved(0);

        new Handler().postDelayed(super::notifyDataSetChanged, 1500);
    }

    @Override
    public Context getContext() {
        return callback.getContext();
    }

    protected class ArrivalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ItemArrivalBinding binding;
        protected ETA_Switcher switcher;

        public ArrivalViewHolder(@NonNull ItemArrivalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.getRoot().setOnClickListener(this);
            this.switcher = new ETA_Switcher();
        }

        @Override
        public void onClick(View view) {
            if (viajes.get(getAdapterPosition()) instanceof ArriboTren){
                ArriboTren item = (ArriboTren) viajes.get(getAdapterPosition());
                callback.onServiceSelected(item.getServiceId());
            }
        }
    }
}
