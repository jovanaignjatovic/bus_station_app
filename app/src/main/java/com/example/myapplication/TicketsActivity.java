package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class TicketsActivity extends AppCompatActivity {

    private ListView ticketListView;
    private TicketAdapter adapter;
    private ArrayList<Ticket> ticketList;
    private FirebaseFirestore firestore;
    private Button returnButton;
    private String startStation;
    private String endStation;
    private Timestamp dateTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);

        ticketListView = findViewById(R.id.ticket);
        returnButton = findViewById(R.id.returnbtn);

        ticketList = new ArrayList<>();
        adapter = new TicketAdapter(this, ticketList);
        ticketListView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        startStation = getIntent().getStringExtra("startstation");
        endStation = getIntent().getStringExtra("endstation");
        dateTimestamp = getIntent().getParcelableExtra("date");

        if (dateTimestamp == null || startStation == null || endStation == null) {
            Toast.makeText(TicketsActivity.this, "Unesite podatke za pretragu karata.", Toast.LENGTH_LONG).show();
            adapter.notifyDataSetChanged();
            return;
        }

        fetchTickets(startStation, endStation, dateTimestamp);

        ticketListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                com.example.myapplication.Ticket selectedTicket = ticketList.get(position);

                if (selectedTicket.getPrice() == 0L || selectedTicket.getStartstation().equals("N/A")) {
                    Toast.makeText(TicketsActivity.this, "Molimo izaberite važeću kartu.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent paymentIntent = new Intent(TicketsActivity.this, PaymentActivity.class);

                paymentIntent.putExtra("startStation", selectedTicket.getStartstation());
                paymentIntent.putExtra("endStation", selectedTicket.getEndstation());
                paymentIntent.putExtra("date", selectedTicket.getFormattedDate());
                paymentIntent.putExtra("time", selectedTicket.getFormattedTime());
                paymentIntent.putExtra("price", String.valueOf(selectedTicket.getPrice()));

                startActivity(paymentIntent);
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TicketsActivity.this, DataActivity.class));
                finish();
            }
        });
    }

    private void fetchTickets(String startStation, String endStation, Timestamp selectedTimestamp) {
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(selectedTimestamp.toDate());
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTime(selectedTimestamp.toDate());
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        Timestamp queryStartTimestamp = new Timestamp(startOfDay.getTime());
        Timestamp queryEndTimestamp = new Timestamp(endOfDay.getTime());

        Timestamp currentExactTimestamp = Timestamp.now();

        firestore.collection("tickets")
                .whereEqualTo("startstation", startStation)
                .whereEqualTo("endstation", endStation)
                .whereGreaterThanOrEqualTo("date", queryStartTimestamp)
                .whereLessThanOrEqualTo("date", queryEndTimestamp)
                .whereGreaterThanOrEqualTo("date", currentExactTimestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ticketList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String fetchedStartSt = doc.getString("startstation");
                        String fetchedEndSt = doc.getString("endstation");
                        Timestamp departureTime = doc.getTimestamp("date");
                        Long price = doc.getLong("price");

                        if (departureTime != null && price != null && fetchedStartSt != null && fetchedEndSt != null) {
                            Ticket ticket = new Ticket(fetchedStartSt, fetchedEndSt, departureTime, price);
                            ticketList.add(ticket);
                        }
                    }

                    if (ticketList.isEmpty()) {
                        Toast.makeText(TicketsActivity.this, "Nema dostupnih karata za odabranu rutu i datum.", Toast.LENGTH_LONG).show();
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TicketsActivity.this, "Greška pri učitavanju karata: ", Toast.LENGTH_SHORT).show();
                    ticketList.clear();
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                });
    }
}