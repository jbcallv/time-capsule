package com.example.timecapsule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
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
        database = FirebaseDatabase.getInstance("https://time-capsule-9f74d-default-rtdb.firebaseio.com").getReference("Users");

        nameEditText = (EditText) findViewById(R.id.activity_register_et_name);
        emailEditText = (EditText) findViewById(R.id.activity_register_et_email);
        passwordEditText = (EditText) findViewById(R.id.activity_register_et_password);
        confirmPasswordEditText = (EditText) findViewById(R.id.activity_register_et_confirm_password);
        registerButton = (Button) findViewById(R.id.activity_register_btn_register);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccountEmailPassword(nameEditText.getText().toString(), emailEditText.getText().toString(),
                        passwordEditText.getText().toString(), confirmPasswordEditText.getText().toString());
            }
        });
    }

    private void createAccountEmailPassword(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            nameEditText.setError("Please enter your name");
            return;
        }
        if (email.isEmpty()) {
            emailEditText.setError("Please enter your email address");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            return;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Please enter a password");
            return;
        }
        if (password.length() < 8) {
            passwordEditText.setError("Password must be greater than 8 characters");
            return;
        }
        if (!password.matches(".*\\d.*")) {
            passwordEditText.setError("Password must contain at least one numeric value");
            return;
        }
        if (!password.matches(".*[!@#$%^&*].*")) {
            passwordEditText.setError("Password must contain at least one special character");
            return;
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Please confirm your password");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Given passwords do not match");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // make sure password is >=6 chars or account creation will fail
                if (task.isSuccessful()) {
                    Log.i(TAG, "account creation successful");

                    User user = new User(name.trim(), email.trim());

                    // add user to database and verify email
                    database.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener <Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                currentUser.sendEmailVerification();
                                Log.i(TAG, "verification email sent");

                                Toast.makeText(RegisterActivity.this,
                                        "Please check your email to verify your account",
                                        Toast.LENGTH_LONG).show();

                                FirebaseAuth.getInstance().signOut();
                                goToLoginScreen();
                            }
                            else {
                                Log.e(TAG, "Couldn't write data to the database");
                            }
                        }
                    });
                }
                else {
                    try {
                        throw task.getException();
                    }
                    catch (FirebaseAuthUserCollisionException emailAlreadyExists) {
                        emailEditText.setError("An account already exists for this email address");
                    }
                    catch (FirebaseAuthWeakPasswordException incorrectPassword) {
                        passwordEditText.setError("Weak password");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
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