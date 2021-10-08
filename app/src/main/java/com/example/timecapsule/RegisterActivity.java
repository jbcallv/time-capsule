package com.example.timecapsule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private FirebaseAuth auth;
    private DatabaseReference database;

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://time-capsule-9f74d-default-rtdb.firebaseio.com/").getReference("Users");

        nameEditText = (EditText) findViewById(R.id.activity_register_et_name);
        emailEditText = (EditText) findViewById(R.id.activity_register_et_email);
        passwordEditText = (EditText) findViewById(R.id.activity_register_et_password);
        confirmPasswordEditText = (EditText) findViewById(R.id.activity_register_et_confirm_password);
        registerButton = (Button) findViewById(R.id.activity_register_btn_register);

        // confirm text in boxes
        // confirm strong password
        // confirm password

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccountEmailPassword(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }

    private void createAccountEmailPassword(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // make sure password is >=6 chars or account creation will fail
                if (task.isSuccessful()) {
                    Log.i(TAG, "account creation successful");
                    // create user object here containing name, email, and anything else

                    // add user to database and verify email
                    database.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                currentUser.sendEmailVerification();
                                Log.i(TAG, "verification email sent");

                                Toast.makeText(RegisterActivity.this,
                                        "Please check your email to verify your account",
                                        Toast.LENGTH_LONG).show();

                                goToLoginScreen();
                            }
                        }
                    });
                }
                else {
                    Log.w(TAG, "account creation unsuccessful");
                    goToLoginScreen();
                }
            }
        });
    }

    private void goToLoginScreen() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}