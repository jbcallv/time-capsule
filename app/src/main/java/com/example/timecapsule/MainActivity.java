package com.example.timecapsule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button signOutButton;
    private Button createCapsuleButton;
    private Button viewCapsulesButton;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signOutButton = (Button) findViewById(R.id.activity_main_btn_sign_out);
        createCapsuleButton = (Button) findViewById(R.id.activity_main_btn_create_capsule);
        viewCapsulesButton = (Button) findViewById(R.id.activity_main_btn_view_capsules);

        //set OnClick listeners for components
        setListeners();

    }

    private void setListeners(){
        createCapsuleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateCapsuleActivity.class);
                startActivity(intent);
                finish();
            }
        });

        viewCapsulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ViewCapsulesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}