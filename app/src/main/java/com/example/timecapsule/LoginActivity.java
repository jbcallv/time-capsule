package com.example.timecapsule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    // conflict test
    private static final String TAG = LoginActivity.class.getSimpleName();

    private FirebaseAuth auth;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button logInButton;
    private TextView forgotPasswordTextView;
    private TextView registerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        emailEditText = (EditText) findViewById(R.id.activity_login_et_email);
        passwordEditText = (EditText) findViewById(R.id.activity_login_et_password);
        logInButton = (Button) findViewById(R.id.activity_login_btn_login);
        forgotPasswordTextView = (TextView) findViewById(R.id.activity_login_tv_forgot_password);
        registerTextView = (TextView) findViewById(R.id.activity_login_tv_register);


        // confirm email and password entered
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(emailEditText.getText().toString().trim(), passwordEditText.getText().toString());
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
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

    private void signIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "signed in successfully");
                    FirebaseUser user = auth.getCurrentUser();

                    // this may get changed later
                    Intent intent = new Intent(LoginActivity.this, CreateCapsuleActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Log.w(TAG, "couldn't sign in");
                }
            }
        });
    }
}