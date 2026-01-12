package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private TextView ticketInfoTextView;
    private EditText cardNumberET;
    private EditText expiryDateET;
    private EditText cvvET;
    private Button payButton;
    private String startStation;
    private String endStation;
    private String date;
    private String time;
    private String price;
    private String userEmail;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        ticketInfoTextView = findViewById(R.id.ticket_info);
        cardNumberET = findViewById(R.id.cardnumber);
        expiryDateET = findViewById(R.id.expirydate);
        cvvET = findViewById(R.id.cvv);
        payButton = findViewById(R.id.pay);
        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPref = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userEmail = sharedPref.getString(MainActivity.USER_EMAIL_KEY, null);

        if (userEmail == null) {
            Toast.makeText(this, "Korisnički email nije pronađen. Prijavite se ponovo.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Intent intent = getIntent();
        if (intent != null) {
            startStation = intent.getStringExtra("startStation");
            endStation = intent.getStringExtra("endStation");
            date = intent.getStringExtra("date");
            time = intent.getStringExtra("time");
            price = intent.getStringExtra("price");

            String ticketInfo = String.format(Locale.getDefault(),
                    "Od: %s\nDo: %s\nDatum: %s\nVreme: %s\nCena: %s",
                    startStation, endStation, date, time, price);
            ticketInfoTextView.setText(ticketInfo);
        } else {
            ticketInfoTextView.setText("Greška pri učitavanju detalja karte.");
        }

        expiryDateET.addTextChangedListener(new TextWatcher() {
            private boolean isEditing;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String input = s.toString().replace("/", "");
                StringBuilder formatted = new StringBuilder();

                if (input.length() >= 2) {
                    formatted.append(input.substring(0, 2));
                    if (input.length() > 2) {
                        formatted.append("/").append(input.substring(2));
                    } else {
                        formatted.append("/");
                    }
                } else {
                    formatted.append(input);
                }

                expiryDateET.setText(formatted.toString());
                expiryDateET.setSelection(expiryDateET.getText().length());

                isEditing = false;
            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payAndEmail();
                startActivity(new Intent(PaymentActivity.this, DataActivity.class));
            }
        });
    }

    private void payAndEmail() {
        String cardNumber = cardNumberET.getText().toString().trim();
        String expiryDate = expiryDateET.getText().toString().trim();
        String cvv = cvvET.getText().toString().trim();

        if (cardNumber.isEmpty() || cardNumber.length() != 16) {
            cardNumberET.setError("Unesite broj kartice.");
            return;
        }
        if (expiryDate.isEmpty() || expiryDate.length() != 5 || !expiryDate.contains("/")) {
            expiryDateET.setError("Unesite datum isteka (MM/GG).");
            return;
        }
        if (cvv.isEmpty() || (cvv.length() != 3 && cvv.length() != 4)) {
            cvvET.setError("Unesite CVV.");
            return;
        }
        Toast.makeText(this, "Plaćanje uspešno", Toast.LENGTH_LONG).show();

        saveTicketToFirestore();

        // confirmationEmail(userEmail);
    }
    private void saveTicketToFirestore() {
        Ticket purchasedTicket = new Ticket(startStation, endStation, date, time, price, userEmail);

        db.collection("purchased")
                .add(purchasedTicket)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PaymentActivity.this, "Karta sačuvana!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PaymentActivity.this, "Greška pri čuvanju kupljene karte: ", Toast.LENGTH_SHORT).show();
                });
    }

   /* private void confirmationEmail(String recipientEmail) {
        String subject = "Potvrda kupovine karte - " + startStation + " do " + endStation;
        String message = String.format(Locale.getDefault(),
                "Poštovani/a,\n\n" +
                        "Uspešno ste kupili kartu sa sledećim detaljima:\n\n" +
                        "  Polazna stanica: %s\n" +
                        "  Odredišna stanica: %s\n" +
                        "  Datum putovanja: %s\n" +
                        "  Vreme polaska: %s\n" +
                        "  Cena: %s\n\n" +
                        "Hvala na poverenju!\n\n" +
                        "Vaš putni servis.",
                startStation, endStation, date, time, price);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(emailIntent, "Pošalji e-mail sa..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Nema instaliranih email klijenata.", Toast.LENGTH_SHORT).show();
        }
    } */
}