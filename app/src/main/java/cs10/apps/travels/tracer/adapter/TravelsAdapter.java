package cs10.apps.travels.tracer.adapter;

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
import cs10.apps.travels.tracer.databinding.ItemTravelBinding;
import cs10.apps.travels.tracer.model.Viaje;

public class TravelsAdapter extends RecyclerView.Adapter<TravelsAdapter.TravelViewHolder> {
    private List<Viaje> viajes;
    private EditTravelCallback callback;

    public void setCallback(EditTravelCallback callback) {
        this.callback = callback;
    }

    public void setViajes(List<Viaje> viajes) {
        this.viajes = viajes;
    }

    public List<Viaje> getViajes() {
        return viajes;
    }

    @NonNull
    @Override
    public TravelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(callback.getContext());
        ItemTravelBinding binding = ItemTravelBinding.inflate(inflater, parent, false);
        return new TravelViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TravelViewHolder holder, int position) {
        Viaje item = viajes.get(position);
        holder.binding.tvDatetime.setText(item.getStartTimeString());
        holder.binding.tvStartPlace.setText(item.getStartAndEnd());
        holder.binding.tvLine.setText(item.getLineInfoAndPrice());
        Drawable icon, bg;

        if (item.getTipo() == 0) {
            icon = AppCompatResources.getDrawable(callback.getContext(), R.drawable.ic_bus);
        } else {
            icon = AppCompatResources.getDrawable(callback.getContext(), R.drawable.ic_train);
        }

        bg = AppCompatResources.getDrawable(callback.getContext(), Utils.colorFor(item.getLinea()));
        holder.binding.ivType.setImageDrawable(icon);
        holder.binding.getRoot().setBackground(bg);
    }

    @Override
    public int getItemCount() {
        return viajes == null ? 0 : viajes.size();
    }

    protected class TravelViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, View.OnLongClickListener {
        protected ItemTravelBinding binding;

        public TravelViewHolder(@NonNull ItemTravelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            callback.onDeleteTravel(viajes.get(getAdapterPosition()).getId(), getAdapterPosition());
            return true;
        }

        @Override
        public void onClick(View view) {
            callback.onEditTravel(viajes.get(getAdapterPosition()).getId());
        }
    }
}
