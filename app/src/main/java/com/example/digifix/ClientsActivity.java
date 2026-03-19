package com.example.digifix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ClientsActivity extends AppCompatActivity {

    private RecyclerView rvClients;
    private ClientsAdapter adapter;
    private List<ClientModel> clientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients);

        setupRecyclerView();
        setupSearch();
        setupNavigation();
    }

    private void setupRecyclerView() {
        rvClients = findViewById(R.id.rvClients);
        rvClients.setLayoutManager(new LinearLayoutManager(this));

        clientList = new ArrayList<>();
        // Mock Data
        clientList.add(new ClientModel("1", "Jane Doe", "+91 98765 43210", "jane@example.com", 2, "JD", true, "₹45,000", 3, "Premium"));
        clientList.add(new ClientModel("2", "John Smith", "+91 91234 56789", "john@example.com", 0, "JS", false, "₹12,500", 1, "Standard"));
        clientList.add(new ClientModel("3", "Alice Brown", "+91 99887 76655", "alice@example.com", 1, "AB", true, "₹28,000", 2, "Gold"));
        clientList.add(new ClientModel("4", "Robert Fox", "+91 98765 12345", "robert@example.com", 0, "RF", false, "₹5,000", 1, "Standard"));
        clientList.add(new ClientModel("5", "Emily White", "+91 91234 98765", "emily@example.com", 3, "EW", true, "₹62,000", 4, "Premium"));
        clientList.add(new ClientModel("6", "Michael Green", "+91 99887 11223", "michael@example.com", 0, "MG", false, "₹8,000", 1, "Standard"));

        // Initialize adapter with full list initially (or copy)
        // Better pattern: keep allClients and filteredClients
        // But simply updating the adapter with a new sublist works too if we keep reference to full list here.
        
        adapter = new ClientsAdapter(new ArrayList<>(clientList));
        rvClients.setAdapter(adapter);
    }
    
    private void setupSearch() {
        android.widget.EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClients(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterClients(String query) {
        String lowerQuery = query.toLowerCase();
        List<ClientModel> filtered = new ArrayList<>();
        
        for (ClientModel client : clientList) {
            if (client.getName().toLowerCase().contains(lowerQuery) || 
                client.getPhone().replaceAll("\\s+", "").contains(lowerQuery) ||
                client.getEmail().toLowerCase().contains(lowerQuery)) {
                filtered.add(client);
            }
        }
        
        if (adapter != null) {
            adapter.updateList(filtered);
        }
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

        // 2. Repairs
        findViewById(R.id.btnNavRepairs).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, RepairsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // 3. Clients (Current)
        findViewById(R.id.btnNavClients).setOnClickListener(v -> {});

        // 4. Settings
        findViewById(R.id.btnNavSettings).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // FAB
        findViewById(R.id.fabMain).setOnClickListener(v -> {
             android.widget.Toast.makeText(this, "Quick Add (Placeholder)", android.widget.Toast.LENGTH_SHORT).show();
        });

        // Swipe Gestures
        findViewById(android.R.id.content).setOnTouchListener(new OnSwipeTouchListener(this) {
             @Override
             public void onSwipeRight() {
                 // Go to Repairs
                 startActivity(new android.content.Intent(ClientsActivity.this, RepairsActivity.class));
                 overridePendingTransition(0, 0);
                 finish();
             }

             @Override
             public void onSwipeLeft() {
                 // Go to Settings
                 startActivity(new android.content.Intent(ClientsActivity.this, SettingsActivity.class));
                 overridePendingTransition(0, 0);
                 finish();
             }
         });
    }
}
