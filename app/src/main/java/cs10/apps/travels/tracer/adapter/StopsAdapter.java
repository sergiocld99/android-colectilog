package cs10.apps.travels.tracer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cs10.apps.travels.tracer.databinding.ItemStopBinding;
import cs10.apps.travels.tracer.model.Parada;

public class StopsAdapter extends RecyclerView.Adapter<StopsAdapter.StopViewHolder> {
    private List<Parada> paradas;
    private EditStopCallback callback;

    public void setCallback(EditStopCallback callback) {
        this.callback = callback;
    }

    public void setParadas(List<Parada> paradas) {
        this.paradas = paradas;
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
        Parada item = paradas.get(position);
        holder.binding.tvName.setText(item.getNombre());
        holder.binding.tvLocation.setText(item.getLatitud() + ", " + item.getLongitud());
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
            callback.onEdit(paradas.get(getAdapterPosition()).getNombre());
        }
    }
}
