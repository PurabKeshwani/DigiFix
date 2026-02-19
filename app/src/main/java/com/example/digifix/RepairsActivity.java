package com.example.digifix;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RepairsActivity extends AppCompatActivity implements RepairsAdapter.OnStatusClickListener, RepairsAdapter.OnItemLongClickListener {

    private RepairsAdapter adapter;
    private List<RepairModel> allRepairs;
    private List<RepairModel> filteredRepairs;
    private TabLayout tabLayout;

    private android.widget.EditText etSearch;
    private String currentSearchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repairs);

        // 1. Setup Data
        allRepairs = new ArrayList<>();
        // Pending/In Progress
        allRepairs.add(new RepairModel("1", "iPhone 13 Pro", "Jane Doe", "+919876543210", "₹15,000", "SN-8829-X", "High", "In Progress", "", "MK", R.drawable.ic_repairs, "in_shop", "Screen Broken & Battery Issue", "Charger, Bag", false, null, null));
        
        // Completed
        RepairModel completedRepair = new RepairModel("2", "Samsung S21", "John Smith", "+919999988888", "₹8,000", "SN-5555", "Normal", "Completed", "", "MK", R.drawable.ic_repairs, "completed", "Charging Port", "None", true, null, null);
        completedRepair.setCompleted(true);
        allRepairs.add(completedRepair);

        allRepairs.add(new RepairModel("3", "iPad Pro 12.9", "Bethany Co.", "+919988776655", "₹22,000", "SN-1122-Z", "Medium", "Waiting for parts", "", "AL", R.drawable.ic_repairs, "at_center", "Logic Board Repair", "Box", true, "Apple Service Nagpur", null));

        filteredRepairs = new ArrayList<>();

        // 2. Setup RecyclerView
        RecyclerView rvRepairs = findViewById(R.id.rvRepairs);
        rvRepairs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RepairsAdapter(filteredRepairs, this, this);
        rvRepairs.setAdapter(adapter);
        
        // 3. Setup Tabs
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterRepairs();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Search
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString();
                filterRepairs();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // Initial Filter (Pending)
        filterRepairs();

        // 4. Setup Swipe to Complete
        setupSwipeGestures(rvRepairs);

        // 5. Navigation Logic
        setupNavigation();
    }
    
    // Updated Filter Method
    private void filterRepairs() {
        boolean showCompleted = tabLayout.getSelectedTabPosition() == 1;
        filteredRepairs.clear();
        String query = currentSearchText.toLowerCase();
        
        for (RepairModel repair : allRepairs) {
            boolean matchesTab = (showCompleted == repair.isCompleted());
            
            boolean matchesSearch = query.isEmpty() || 
                                    repair.getCustomerInfo().toLowerCase().contains(query) ||
                                    repair.getDeviceName().toLowerCase().contains(query) ||
                                    (repair.getSerialNumber() != null && repair.getSerialNumber().toLowerCase().contains(query));
            
            if (matchesTab && matchesSearch) {
                filteredRepairs.add(repair);
            }
        }
        adapter.notifyDataSetChanged();
    }
    
    // Legacy filter method for compatibility if called elsewhere
    private void filterRepairs(boolean showCompleted) {
         // This is now handled by the main filterRepairs() using tabLayout state,
         // but strictly speaking if we pass an arg we might want to respect it. 
         // For now, let's just delegate to the main one which pulls from Tabs.
         // Or update tab selection?
         // Simplest is to just call filterRepairs() which reads tab state.
         filterRepairs();
    }

    private void setupSwipeGestures(RecyclerView rv) {
        // Swipe to Complete (Right) & Send Back to Pending (Left)
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            
            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                // Basic check to avoid crashes if list updates while swiping
                if (position < 0 || position >= filteredRepairs.size()) return 0;
                
                RepairModel repair = filteredRepairs.get(position);
                if (repair.isCompleted()) {
                    // If Completed, ONLY allow Left Swipe (Restore)
                    return ItemTouchHelper.LEFT;
                } else {
                    // If Pending, ONLY allow Right Swipe (Complete)
                    return ItemTouchHelper.RIGHT;
                }
            }
            
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                RepairModel repair = filteredRepairs.get(position);
                
                if (direction == ItemTouchHelper.RIGHT) {
                    // Mark as Complete logic
                    showCompleteConfirmation(repair, position);
                } else if (direction == ItemTouchHelper.LEFT) {
                     // Send back to Pending
                    repair.setCompleted(false); // also updates status string
                    filterRepairs(tabLayout.getSelectedTabPosition() == 1); // Refresh list
                    Toast.makeText(RepairsActivity.this, "Sent back to Pending", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Paint p = new Paint();
                    if (dX > 0) {
                        // Right Swipe (Green / Complete)
                        p.setColor(0xFF4CAF50); // Green
                        c.drawRoundRect(new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom()), 16, 16, p);
                    } else if (dX < 0) {
                        // Left Swipe (Orange / Restore)
                        p.setColor(0xFFFF9800); // Orange
                        c.drawRoundRect(new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom()), 16, 16, p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rv);
        
        // Horizontal Navigation Gestures (Activity switching)
        // Note: RecyclerView consumes touch events, so standard OnTouchListener on root might get blocked.
        // But for now keeping the root listener for consistency.
        findViewById(android.R.id.content).setOnTouchListener(new OnSwipeTouchListener(this) {
             @Override
            public void onSwipeRight() {
                 // Prevent conflict with item swipe? 
                 // Actually this handles Activity Navigation only if touch is on Root.
                 // We'll leave it simple for now. 
                 super.onSwipeRight(); 
                 // Explicitly:
                android.content.Intent intent = new android.content.Intent(RepairsActivity.this, DashboardActivity.class);
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
            
             @Override
            public void onSwipeLeft() {
                startActivity(new android.content.Intent(RepairsActivity.this, ClientsActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    private void showCompleteConfirmation(RepairModel repair, int position) {
        // Simple Dialog for confirmation
        BottomSheetDialog sheet = new BottomSheetDialog(this, com.google.android.material.R.style.Theme_Design_BottomSheetDialog);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        layout.setBackgroundColor(0xFF1E1E1E);

        TextView title = new TextView(this);
        title.setText("MARK AS COMPLETE?");
        title.setTextSize(18);
        title.setTextColor(0xFFFFFFFF);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(android.view.Gravity.CENTER);
        layout.addView(title);
        
        TextView sub = new TextView(this);
        sub.setText("Is " + repair.getCustomerInfo() + "'s device ready?");
        sub.setTextColor(0xFFAAAAAA);
        sub.setGravity(android.view.Gravity.CENTER);
        sub.setPadding(0, 16, 0, 48);
        layout.addView(sub);
        
        // Buttons
        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        
        android.widget.Button btnCancel = new android.widget.Button(this);
        btnCancel.setText("CANCEL");
        btnCancel.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        btnCancel.setOnClickListener(v -> {
            adapter.notifyItemChanged(position); // Restore item
            sheet.dismiss();
        });
        
        android.widget.Button btnConfirm = new android.widget.Button(this);
        btnConfirm.setText("YES, COMPLETE");
        btnConfirm.setTextColor(0xFFFFFFFF);
        btnConfirm.setBackgroundColor(0xFF4CAF50); // Green
        btnConfirm.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        btnConfirm.setOnClickListener(v -> {
            repair.setCompleted(true);
            filterRepairs(tabLayout.getSelectedTabPosition() == 1); // Refresh list
            sheet.dismiss();
            Toast.makeText(this, "Marked as Completed", Toast.LENGTH_SHORT).show();
        });
        
        btns.addView(btnCancel);
        btns.addView(btnConfirm);
        layout.addView(btns);
        
        sheet.setContentView(layout);
        sheet.setCancelable(false);
        sheet.show();
    }

    private void setupNavigation() {
        // 1. Home
        findViewById(R.id.btnNavHome).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, DashboardActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        // 2. Repairs (Already Here)
        findViewById(R.id.btnNavRepairs).setOnClickListener(v -> {});

        // 3. Clients
        findViewById(R.id.btnNavClients).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ClientsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // 4. Settings
        findViewById(R.id.btnNavSettings).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // FAB
        findViewById(R.id.fabMain).setOnClickListener(v -> {
             Toast.makeText(this, "Quick Add (Placeholder)", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStatusClick(RepairModel repair) {
        // Reuse Long Press Action Sheet for now, or just show details
        showActionSheet(repair);
    }

    @Override
    public void onItemLongClick(RepairModel repair) {
        showActionSheet(repair);
    }

    private void showActionSheet(RepairModel repair) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, com.google.android.material.R.style.Theme_Design_BottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.layout_repair_action_sheet, null);
        
        // Header
        TextView tvName = view.findViewById(R.id.tvSheetCustomerName);
        TextView tvDevice = view.findViewById(R.id.tvSheetDeviceName);
        
        tvName.setText(repair.getCustomerInfo());
        tvDevice.setText(repair.getDeviceName() + " • " + (repair.getSerialNumber() != null ? repair.getSerialNumber() : "No SN"));
        
        // Actions
        LinearLayout actionCall = view.findViewById(R.id.actionCall);
        LinearLayout actionWhatsApp = view.findViewById(R.id.actionWhatsApp);
        LinearLayout actionWarranty = view.findViewById(R.id.actionWarranty);
        LinearLayout actionComplete = view.findViewById(R.id.actionComplete);
        LinearLayout actionDelete = view.findViewById(R.id.actionDelete);
        
        actionCall.setOnClickListener(v -> {
             dialog.dismiss();
             Intent intent = new Intent(Intent.ACTION_DIAL);
             intent.setData(Uri.parse("tel:" + repair.getCustomerMobile()));
             startActivity(intent);
        });
        
        actionWhatsApp.setOnClickListener(v -> {
             dialog.dismiss();
             try {
                 String url = "https://api.whatsapp.com/send?phone=" + repair.getCustomerMobile();
                 Intent i = new Intent(Intent.ACTION_VIEW);
                 i.setData(Uri.parse(url));
                 startActivity(i);
             } catch (Exception e) {
                 Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
             }
        });
        
        // Conditional Visibility
        if (repair.isCompleted()) {
             actionWarranty.setVisibility(View.GONE);
             actionComplete.setVisibility(View.GONE);
        } else {
             actionWarranty.setVisibility(View.VISIBLE);
             actionComplete.setVisibility(View.VISIBLE);
             
             actionWarranty.setOnClickListener(v -> {
                 dialog.dismiss();
                 Toast.makeText(this, "Sending to Warranty...", Toast.LENGTH_SHORT).show();
             });
             
             actionComplete.setOnClickListener(v -> {
                 dialog.dismiss();
                 repair.setCompleted(true);
                 filterRepairs(tabLayout.getSelectedTabPosition() == 1);
             });
        }
        
        actionDelete.setOnClickListener(v -> {
             dialog.dismiss();
             allRepairs.remove(repair);
             filterRepairs(tabLayout.getSelectedTabPosition() == 1);
             Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
        });
        
        dialog.setContentView(view);
        
        // Transparent background for custom rounded corners to show
        if (dialog.getWindow() != null) {
            dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        }
        
        dialog.show();
    }
}
