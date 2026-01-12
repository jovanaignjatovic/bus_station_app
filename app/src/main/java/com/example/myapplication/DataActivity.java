package com.example.myapplication;

import com.example.myapplication.R;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataActivity extends AppCompatActivity {

    private Spinner startStationSpinner;
    private Spinner endStationSpinner;
    private Button dateButton;
    private Button findButton;
    private FirebaseFirestore firestore;
    private Button clearButton;
    private DatePickerDialog datePickerDialog;
    private Calendar selectedDateCalendar;
    private RecyclerView purchasedTicketsRecyclerView;
    private com.example.myapplication.purchasedAdapter purchasedTicketAdapter;
    private List<Ticket> purchasedTicketList;
    private String userEmail;
    Button profilebtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        startStationSpinner = findViewById(R.id.startstation);
        endStationSpinner = findViewById(R.id.endstation);
        dateButton = findViewById(R.id.date);
        findButton = findViewById(R.id.find);
        clearButton = findViewById(R.id.delete);
        profilebtn = findViewById(R.id.profilebtn);
        firestore = FirebaseFirestore.getInstance();

        selectedDateCalendar = Calendar.getInstance();
        dateButton.setText(formattedDate(selectedDateCalendar));
        datePicker();

        purchasedTicketsRecyclerView = findViewById(R.id.ticketsRecyclerView);
        purchasedTicketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        purchasedTicketList = new ArrayList<>();

        purchasedTicketAdapter = new purchasedAdapter(purchasedTicketList);
        purchasedTicketsRecyclerView.setAdapter(purchasedTicketAdapter);

        SharedPreferences sharedPref = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userEmail = sharedPref.getString(MainActivity.USER_EMAIL_KEY, null);

        setupStationSpinners();
        findButtonListener();
        setupClearButtonListener();

        if (userEmail != null) {
            loadUserPurchasedTickets();
        }

        profilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DataActivity.this, ProfileActivity.class));
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (userEmail != null) {
            loadUserPurchasedTickets();
        }
    }

    private String formattedDate(Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return String.format("%02d.%02d.%d", day, month, year);
    }

    private void datePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
            selectedDateCalendar.set(year, month, day);
            dateButton.setText(formattedDate(selectedDateCalendar));
        };

        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);

        Calendar minDate = Calendar.getInstance();
        minDate.set(year, Calendar.JULY, 1);

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(year, Calendar.SEPTEMBER, 31);

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
    }

    public void openDatePicker(View view) {
        datePickerDialog.show();
    }

    private void setupStationSpinners() {
        List<String> stationList = new ArrayList<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stationList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        startStationSpinner.setAdapter(adapter);
        endStationSpinner.setAdapter(adapter);

        firestore.collection("stations").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    stationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String stationName = doc.getString("station");
                        if (stationName != null) {
                            stationList.add(stationName);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DataActivity.this, "Greška pri učitavanju stanica: ", Toast.LENGTH_SHORT).show();
                });
    }

    private void findButtonListener() {
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedStart = startStationSpinner.getSelectedItem().toString();
                String selectedEnd = endStationSpinner.getSelectedItem().toString();

                selectedDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                selectedDateCalendar.set(Calendar.MINUTE, 0);
                selectedDateCalendar.set(Calendar.SECOND, 0);
                selectedDateCalendar.set(Calendar.MILLISECOND, 0);

                Timestamp timestamp = new Timestamp(selectedDateCalendar.getTime());

                Intent intent = new Intent(DataActivity.this, TicketsActivity.class);
                intent.putExtra("startstation", selectedStart);
                intent.putExtra("endstation", selectedEnd);
                intent.putExtra("date", timestamp);
                startActivity(intent);
            }
        });
    }

    private void setupClearButtonListener() {
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelections();
            }
        });
    }
    private void clearSelections() {
        if (startStationSpinner != null && endStationSpinner != null) {
            startStationSpinner.setSelection(0);
            endStationSpinner.setSelection(0);
        }

        selectedDateCalendar = Calendar.getInstance();
        dateButton.setText(formattedDate(selectedDateCalendar));
    }

    private void loadUserPurchasedTickets() {
        firestore.collection("purchased")
                .whereEqualTo("userEmail", userEmail)
                .orderBy("formattedDate", Query.Direction.DESCENDING)
                .orderBy("formattedTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {
                            purchasedTicketList.clear();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Ticket ticket = document.toObject(Ticket.class);
                                if (ticket != null) { purchasedTicketList.add(ticket);
                                } else {
                                }
                            }
                            purchasedTicketAdapter.notifyDataSetChanged();
                            if (purchasedTicketList.isEmpty()) {
                                Toast.makeText(DataActivity.this, "Nemate kupljenih karata.", Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(e -> { Toast.makeText(DataActivity.this, "Greška pri učitavanju kupljenih karata: ", Toast.LENGTH_SHORT).show();
                });
    }

}