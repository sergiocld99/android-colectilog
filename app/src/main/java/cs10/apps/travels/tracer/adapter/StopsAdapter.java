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
import cs10.apps.travels.tracer.databinding.ItemStopBinding;
import cs10.apps.travels.tracer.model.ScheduledParada;
import cs10.apps.travels.tracer.ui.UpsideDownSwitcher;

public class StopsAdapter extends RecyclerView.Adapter<StopsAdapter.StopViewHolder> {
    private List<ScheduledParada> paradas;
    private EditStopCallback callback;

    public void setCallback(EditStopCallback callback) {
        this.callback = callback;
    }

    public void setParadas(@NonNull List<ScheduledParada> paradas) {
        this.paradas = paradas;
    }

    public List<ScheduledParada> getParadas() {
        return paradas;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(callback.getContext());
        ItemStopBinding binding = ItemStopBinding.inflate(inflater, parent, false);
        return new StopViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        ScheduledParada item = paradas.get(position);
        holder.binding.tvName.setText(item.getNombre());
        Drawable icon, bg;

        if (item.getTipo() == 0) {
            icon = AppCompatResources.getDrawable(callback.getContext(), R.drawable.ic_bus);
        } else {
            icon = AppCompatResources.getDrawable(callback.getContext(), R.drawable.ic_train);
        }

        bg = AppCompatResources.getDrawable(callback.getContext(), Utils.colorFor(item.getLinea()));
        holder.binding.ivType.setImageDrawable(icon);
        holder.binding.getRoot().setBackground(bg);

        if (position < 3 && item.getRamal() != null){
            holder.uds.setContext(callback.getContext());
            holder.uds.setItem(item);
            holder.uds.setTvSwitcher(holder.binding.tvSwitcher);
            holder.uds.startAnimation();
            holder.binding.tvLocation.setVisibility(View.GONE);
            holder.binding.tvSwitcher.setVisibility(View.VISIBLE);
        } else {
            holder.uds.stop();
            holder.binding.tvLocation.setText(callback.getContext().getString(R.string.next_to, item.getLineaAsString(), item.getNextArrival()));
            holder.binding.tvLocation.setVisibility(View.VISIBLE);
            holder.binding.tvSwitcher.setVisibility(View.GONE);
        }

        // always
        holder.binding.tvStartCount.setText(callback.getContext().getString(R.string.destination, item.getNombrePdaFin()));
    }

    @Override
    public int getItemCount() {
        return paradas == null ? 0 : paradas.size();
    }

    protected class StopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ItemStopBinding binding;
        protected UpsideDownSwitcher uds;

        public StopViewHolder(@NonNull ItemStopBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(this);
            uds = new UpsideDownSwitcher();
        }

        @Override
        public void onClick(View view) {
            callback.onEditStop(paradas.get(getAdapterPosition()).getNombre());
        }
    }
}
