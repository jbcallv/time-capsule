package com.example.timecapsule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText capsuleDataEditText;
    private Button saveButton;

    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance("https://time-capsule-9f74d-default-rtdb.firebaseio.com/").getReference();
        Log.i(TAG, "connected to database");

        capsuleDataEditText = (EditText) findViewById(R.id.activity_main_et_data);
        saveButton = (Button) findViewById(R.id.activity_main_btn_save);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.child("test").setValue(capsuleDataEditText.getText().toString());
                Log.i(TAG, "pushed values to database");
            }
        });
    }
}