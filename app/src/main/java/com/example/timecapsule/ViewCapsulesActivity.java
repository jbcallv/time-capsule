package com.example.timecapsule;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ViewCapsulesActivity extends AppCompatActivity {

    // help on downloading files:
    // https://stackoverflow.com/questions/39905719/how-to-download-a-file-from-firebase-storage-to-the-external-storage-of-android

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference database;

    private Button mainMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_capsules);
        mainMenuButton = findViewById(R.id.mainMenuButton);

        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewCapsulesActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Log.v("Jeannine", "WHY");
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance("https://time-capsule-9f74d-default-rtdb.firebaseio.com").getReference("Users");
        Log.v("Jeannine", "hello wolrd");
        Log.v("Jeannine", currentUser.getUid());

        Log.v("Jeannine", String.valueOf(database.child("Users").child(currentUser.getUid()).child("capsules").get()));

        database.child(currentUser.getUid()).child("capsules").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    int i = 0;
                    for(DataSnapshot child : task.getResult().getChildren()){
                        Log.d("firebase", String.valueOf(i));
                        i++;
                        Log.d("firebase", child.getValue().toString());

                    }
                }
            }
        });

        Button myButton = new Button(this);
        myButton.setText("Hi");
        LinearLayout layout = (LinearLayout) findViewById(R.id.activity_view_layout);
        layout.addView(myButton);

    }


}
