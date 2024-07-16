package com.example.transpomatetest2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private Spinner spinnerRoutes;
    private Spinner spinnerBuses;
    private Button buttonViewBus;
    private DatabaseReference databaseReference;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<String> routeList = new ArrayList<>();
    private List<String> busList = new ArrayList<>();
    private ArrayAdapter<String> routeAdapter;
    private ArrayAdapter<String> busAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerRoutes = findViewById(R.id.spinnerRoutes);
        spinnerBuses = findViewById(R.id.spinnerBuses);
        buttonViewBus = findViewById(R.id.buttonViewBus);

        databaseReference = FirebaseDatabase.getInstance().getReference("buses");

        // Setup route spinner adapter
        routeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, routeList);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoutes.setAdapter(routeAdapter);

        // Setup bus spinner adapter
        busAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, busList);
        busAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBuses.setAdapter(busAdapter);

        // Load routes from Firebase
        loadRoutes();

        spinnerRoutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRoute = parent.getItemAtPosition(position).toString();
                loadBusesForRoute(selectedRoute);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        buttonViewBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedRoute = spinnerRoutes.getSelectedItem().toString();
                String selectedBus = spinnerBuses.getSelectedItem().toString();
                fetchBusData(selectedRoute, selectedBus);
            }
        });

        // Request location permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void loadRoutes() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                routeList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot routeSnapshot : dataSnapshot.getChildren()) {
                        String routeId = routeSnapshot.getKey();
                        routeList.add(routeId);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No routes available", Toast.LENGTH_SHORT).show();
                }
                routeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load routes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBusesForRoute(String route) {
        databaseReference.child(route).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                busList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot busSnapshot : dataSnapshot.getChildren()) {
                        String busId = busSnapshot.getKey();
                        busList.add(busId);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No buses available for the selected route", Toast.LENGTH_SHORT).show();
                }
                busAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load buses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBusData(String route, String bus) {
        databaseReference.child(route).child(bus).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String busInfo = dataSnapshot.child("info").getValue(String.class);
                    int seatsAvailable = dataSnapshot.child("seatsAvailable").getValue(Integer.class);
                    double latitude = dataSnapshot.child("location").child("lat").getValue(Double.class);
                    double longitude = dataSnapshot.child("location").child("lng").getValue(Double.class);

                    Intent intent = new Intent(MainActivity.this, BusDetailsActivity.class);
                    intent.putExtra("busDetails", busInfo);
                    intent.putExtra("seatsAvailable", String.valueOf(seatsAvailable));
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "No data available for the selected bus", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with accessing location
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, disable location functionality
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}