package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView regTextView;

    private FirebaseAuth mAuth;
    // private FirebaseFirestore firestore;
     public static final String PREFS_NAME = "MyPrefsFile";
     public static final String USER_EMAIL_KEY = "user_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        // firestore = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        regTextView = findViewById(R.id.registration);

        setupLoginButton();
        setupRegistrationButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(MainActivity.this, "Vec ste prijavljeni!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, DataActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strEmail = emailEditText.getText().toString().trim();
                String strPassword = passwordEditText.getText().toString().trim();
                if (strEmail.isEmpty() || strPassword.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Unesite email i lozinku", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(strEmail, strPassword)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(MainActivity.this, "Uspešna prijava!", Toast.LENGTH_SHORT).show();
                                    SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString(USER_EMAIL_KEY, user.getEmail());
                                    editor.apply();
                                    Intent intent = new Intent(MainActivity.this, DataActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String errorMessage = "Prijava neuspešna: ";
                                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    private void setupRegistrationButton() {
        regTextView.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, registrationActivity.class));
        });
    }
}