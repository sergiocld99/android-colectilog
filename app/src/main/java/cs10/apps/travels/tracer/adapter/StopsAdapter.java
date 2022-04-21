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
        holder.binding.tvLocation.setText(item.getLatitud() + ", " + item.getLongitud());
        holder.binding.tvStartCount.setText("Next is " + item.getLinea() + " at " + item.getStartHour() + ":" + Utils.twoDecimals(item.getStartMinute()));
        //holder.binding.tvStartCount.setText(item.getVeces() + " veces");
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
        return paradas == null ? 0 : paradas.size();
    }

    protected class StopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ItemStopBinding binding;

        public StopViewHolder(@NonNull ItemStopBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            callback.onEditStop(paradas.get(getAdapterPosition()).getNombre());
        }
    }
}
