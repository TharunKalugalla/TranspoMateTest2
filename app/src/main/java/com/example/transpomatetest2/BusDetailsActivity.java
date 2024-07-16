package com.example.transpomatetest2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BusDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView textViewBusDetails;
    private TextView textViewSeatsAvailable;
    private Button buttonReserveSeat;
    private Button buttonViewBusLocation;
    private GoogleMap mMap;
    private LatLng busLocation;
    private boolean mapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_details);

        textViewBusDetails = findViewById(R.id.textViewBusDetails);
        textViewSeatsAvailable = findViewById(R.id.textViewSeatsAvailable);
        buttonReserveSeat = findViewById(R.id.buttonReserveSeat);
        buttonViewBusLocation = findViewById(R.id.buttonViewBusLocation);

        Intent intent = getIntent();
        String busDetails = intent.getStringExtra("busDetails");
        String seatsAvailable = intent.getStringExtra("seatsAvailable");
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);

        busLocation = new LatLng(latitude, longitude);

        textViewBusDetails.setText(busDetails);
        textViewSeatsAvailable.setText("Seats Available: " + seatsAvailable);

        buttonReserveSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement seat reservation logic
            }
        });

        buttonViewBusLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady) {
                    findViewById(R.id.mapContainer).setVisibility(View.VISIBLE);
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(busLocation).title("Bus Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busLocation, 15));
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
    }
}