package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    EditText nameET;
    EditText lastNameET;
    EditText emailET;
    Button passwordButton;
    Button saveButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        nameET = findViewById(R.id.ime);
        lastNameET = findViewById(R.id.prezime);
        emailET = findViewById(R.id.email);
        passwordButton = findViewById(R.id.passwordchange);
        saveButton = findViewById(R.id.save);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    ProfileActivity.this.currentUser = user;
                    userProfile();
                    emailET.setEnabled(false);
                } else {
                    ProfileActivity.this.currentUser = null;
                    Toast.makeText(ProfileActivity.this, "Molimo prijavite se ponovo.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    saveUser();
                    startActivity(new Intent(ProfileActivity.this, DataActivity.class));
                    finish();
                } else {
                    Toast.makeText(ProfileActivity.this, "Molimo sačekajte dok se profil učita ili se prijavite.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    sendResetEmail();
                } else {
                    Toast.makeText(ProfileActivity.this, "Nema prijavljenog korisnika za promenu lozinke.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth != null && mAuthStateListener != null) {
            mAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuth != null && mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void userProfile() {
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("Name");
                        String surname = document.getString("Last name");

                        nameET.setText(name);
                        lastNameET.setText(surname);
                        emailET.setText(currentUser.getEmail());

                    } else {
                        Toast.makeText(ProfileActivity.this, "Podaci profila nisu pronađeni. Molimo sačuvajte.", Toast.LENGTH_LONG).show();
                        emailET.setText(currentUser.getEmail());
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Greška pri učitavanju profila.", Toast.LENGTH_SHORT).show();
                    emailET.setText(currentUser.getEmail());
                }
            }
        });
    }

    private void saveUser() {
        String newName = nameET.getText().toString().trim();
        String newSurname = lastNameET.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("Name", newName);
        updates.put("Last name", newSurname);

        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        userRef.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileActivity.this, "Profil uspešno sačuvan!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Greška pri čuvanju profila: ", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendResetEmail() {
        if (currentUser == null || currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {
            Toast.makeText(this, "Nema dostupnog emaila za resetovanje lozinke.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(currentUser.getEmail())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Poslata je poruka za resetovanje lozinke na vaš email. Proverite inbox.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Greška pri slanju poruke za resetovanje lozinke: ", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}