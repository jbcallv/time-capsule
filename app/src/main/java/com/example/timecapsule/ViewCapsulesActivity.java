package com.example.timecapsule;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance("https://time-capsule-9f74d-default-rtdb.firebaseio.com").getReference("Users");

        database.child(currentUser.getUid()).child("capsules").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {

                    Date currentDate = Calendar.getInstance().getTime();

                    for(DataSnapshot child : task.getResult().getChildren()){
                        String key = child.getKey();
                        Map<String, Object> childData = (Map<String, Object>)child.getValue();
                        Button myButton = new Button(ViewCapsulesActivity.this);

                        Date childDate;
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.bottomMargin = 9;
                        try {
                            childDate =new SimpleDateFormat("MM/dd/yyyy HH:mm").parse(childData.get("opendatetime").toString());

                            if(childDate.compareTo(currentDate) > 0){
                                String buttonText = "LOCKED until: " + childData.get("opendatetime").toString();
                                myButton.setText(buttonText);
                            }
                            else{
                                myButton.setText(childData.get("title").toString());
                                myButton.setBackgroundColor(Color.CYAN);
                                myButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(ViewCapsulesActivity.this, ViewSingleCapsuleActivity.class);
                                        intent.putExtra("dataKey", key);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        LinearLayout layout = findViewById(R.id.activity_view_layout);
                        layout.addView(myButton, params);

                    }
                }

            }

        });

    }


}
