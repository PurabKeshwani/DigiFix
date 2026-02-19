package com.example.digifix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digifix.R;
import java.util.List;

public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ClientViewHolder> {

    private List<ClientModel> clientList;

    public ClientsAdapter(List<ClientModel> clientList) {
        this.clientList = clientList;
    }

    public void updateList(List<ClientModel> newList) {
        this.clientList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_card, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        ClientModel client = clientList.get(position);

        holder.tvInitials.setText(client.getInitials());
        holder.tvClientName.setText(client.getName());
        holder.tvPhone.setText(client.getPhone());
        
        // Status Dot logic
        if (client.isOnline()) {
            holder.viewStatusDot.setBackgroundResource(R.drawable.bg_notification_dot); // Green/Orange dot
        } else {
            holder.viewStatusDot.setVisibility(View.GONE);
        }

        // Active Jobs Badge
        if (client.getActiveJobsCount() > 0) {
            holder.tvActiveJobs.setVisibility(View.VISIBLE);
            holder.tvActiveJobs.setText(client.getActiveJobsCount() + " Active");
        } else {
            holder.tvActiveJobs.setVisibility(View.GONE);
        }

        // Rich Stats Binding
        holder.tvMembership.setText(client.getMembershipType());
        holder.tvTotalSpent.setText("Lifetime: " + client.getTotalSpent());
        holder.tvDeviceCount.setText(client.getDeviceCount() + " Devices");

        // Click Listeners
        holder.btnCall.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Calling " + client.getName(), Toast.LENGTH_SHORT).show();
        });

        holder.btnMessage.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Messaging " + client.getName(), Toast.LENGTH_SHORT).show();
        });
        
        holder.itemView.setOnClickListener(v -> {
             // Open Details
        });
    }

    @Override
    public int getItemCount() {
        return clientList.size();
    }

    public static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvClientName, tvPhone, tvActiveJobs;
        TextView tvMembership, tvTotalSpent, tvDeviceCount; // New Stats
        View viewStatusDot;
        View btnCall, btnMessage; // Changed to View (FrameLayout)

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvActiveJobs = itemView.findViewById(R.id.tvActiveJobs);
            viewStatusDot = itemView.findViewById(R.id.viewStatusDot);
            
            // New Stats
            tvMembership = itemView.findViewById(R.id.tvMembership);
            tvTotalSpent = itemView.findViewById(R.id.tvTotalSpent);
            tvDeviceCount = itemView.findViewById(R.id.tvDeviceCount);

            btnCall = itemView.findViewById(R.id.btnCall);
            btnMessage = itemView.findViewById(R.id.btnMessage);
        }
    }
}
