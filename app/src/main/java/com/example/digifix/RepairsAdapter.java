package com.example.digifix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RepairsAdapter extends RecyclerView.Adapter<RepairsAdapter.RepairViewHolder> {

    private List<RepairModel> repairList;
    private OnStatusClickListener statusClickListener;
    private OnItemLongClickListener itemLongClickListener;

    public interface OnStatusClickListener {
        void onStatusClick(RepairModel repair);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(RepairModel repair);
    }

    public RepairsAdapter(List<RepairModel> repairList, OnStatusClickListener statusListener, OnItemLongClickListener longListener) {
        this.repairList = repairList;
        this.statusClickListener = statusListener;
        this.itemLongClickListener = longListener;
    }
    
    public void updateList(List<RepairModel> newList) {
        this.repairList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RepairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_repair_card, parent, false);
        return new RepairViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RepairViewHolder holder, int position) {
        RepairModel repair = repairList.get(position);

        // Header
        holder.tvCustomerName.setText(repair.getCustomerInfo().toUpperCase()); 
        holder.tvDeviceName.setText(repair.getDeviceName().toUpperCase());
        holder.imgVerified.setVisibility(repair.isWarranty() ? View.VISIBLE : View.GONE);

        // Status Strip & Styling
        int statusColor = repair.isCompleted() ? 0xFF4CAF50 : 0xFFFF9800; // Green : Orange
        holder.viewStatusStrip.setBackgroundColor(statusColor);
        holder.tvDeviceName.setTextColor(statusColor);

        // Body: Issue
        if (repair.getIssue() != null && !repair.getIssue().isEmpty()) {
            holder.tvIssue.setText(repair.getIssue());
            holder.layoutIssue.setVisibility(View.VISIBLE);
            
             // Expand logic
            holder.layoutIssue.setOnClickListener(v -> {
                 if (holder.tvIssue.getMaxLines() == 1) {
                     holder.tvIssue.setMaxLines(10);
                 } else {
                     holder.tvIssue.setMaxLines(1);
                 }
            });
        } else {
            holder.layoutIssue.setVisibility(View.GONE);
        }

        // Body: Accessories
        if (repair.getAccessories() != null && !repair.getAccessories().isEmpty() && !repair.getAccessories().equalsIgnoreCase("None")) {
            holder.tvAccessories.setText(repair.getAccessories().toUpperCase());
            holder.layoutAccessories.setVisibility(View.VISIBLE);
        } else {
             holder.layoutAccessories.setVisibility(View.GONE);
        }

        // Body: Center Info / Repair Stage
        String stage = repair.getRepairStage(); // "in_shop", "at_center", "completed"
        if ("at_center".equals(stage)) {
            holder.layoutCenterInfo.setVisibility(View.VISIBLE);
            holder.tvCenterInfo.setText("At " + (repair.getCenterName() != null ? repair.getCenterName().toUpperCase() : "CENTER"));
            holder.tvCenterInfo.setTextColor(0xFF7C4DFF); // Deep Purple
            holder.imgCenterIcon.setColorFilter(0xFF7C4DFF);
            holder.imgCenterIcon.setImageResource(R.drawable.ic_store);
        } else if ("received_back".equals(stage)) { 
             holder.layoutCenterInfo.setVisibility(View.VISIBLE);
             String outcome = repair.getWarrantyOutcome() != null ? repair.getWarrantyOutcome() : "Returned";
             holder.tvCenterInfo.setText("Received: " + outcome.toUpperCase());
             
             int color = 0xFFFF9800; // Default Orange
             if ("replaced".equalsIgnoreCase(outcome)) color = 0xFF4CAF50; // Green
             else if ("rejected".equalsIgnoreCase(outcome)) color = 0xFFF44336; // Red
             
             holder.tvCenterInfo.setTextColor(color);
             holder.imgCenterIcon.setColorFilter(color);
             holder.imgCenterIcon.setImageResource(R.drawable.ic_verified); 
        } else {
            holder.layoutCenterInfo.setVisibility(View.GONE);
        }

        // Footer
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.US);
            // Use current time if 0
            long time = repair.getTimestamp() > 0 ? repair.getTimestamp() : System.currentTimeMillis();
            holder.tvDate.setText(sdf.format(new Date(time)).toUpperCase());
        } catch (Exception e) {
            holder.tvDate.setText("DATE INFO");
        }
        
        holder.tvPrice.setText(repair.getEstimatedPrice());
        holder.tvAddedBy.setText(": " + (repair.getAddedBy() != null ? repair.getAddedBy().toUpperCase() : "MK"));

        // Interaction
        holder.cardContainer.setOnClickListener(v -> {
            if (statusClickListener != null) {
                statusClickListener.onStatusClick(repair);
            }
        });
        
        holder.cardContainer.setOnLongClickListener(v -> {
             if (itemLongClickListener != null) {
                 itemLongClickListener.onItemLongClick(repair);
             }
             return true;
        });
    }

    @Override
    public int getItemCount() {
        return repairList.size();
    }

    public static class RepairViewHolder extends RecyclerView.ViewHolder {
        
        View viewStatusStrip;
        TextView tvCustomerName, tvDeviceName, tvIssue, tvAccessories, tvCenterInfo, tvDate, tvPrice, tvAddedBy;
        ImageView imgVerified, imgCenterIcon;
        LinearLayout layoutIssue, layoutAccessories, layoutCenterInfo;
        View cardContainer; // Changed from ConstraintLayout to View (generic) or MaterialCardView if needed

        public RepairViewHolder(@NonNull View itemView) {
            super(itemView);
            // Header
            viewStatusStrip = itemView.findViewById(R.id.viewStatusStrip);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            imgVerified = itemView.findViewById(R.id.imgVerified);
            
            // Body
            layoutIssue = itemView.findViewById(R.id.layoutIssue);
            tvIssue = itemView.findViewById(R.id.tvIssue);
            layoutAccessories = itemView.findViewById(R.id.layoutAccessories);
            tvAccessories = itemView.findViewById(R.id.tvAccessories);
            layoutCenterInfo = itemView.findViewById(R.id.layoutCenterInfo);
            tvCenterInfo = itemView.findViewById(R.id.tvCenterInfo);
            imgCenterIcon = itemView.findViewById(R.id.imgCenterIcon);
            
            // Footer
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvAddedBy = itemView.findViewById(R.id.tvAddedBy);
            
            cardContainer = itemView.findViewById(R.id.cardContainer);
        }
    }
}
