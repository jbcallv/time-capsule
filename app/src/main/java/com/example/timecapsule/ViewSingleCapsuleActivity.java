package com.example.timecapsule;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class ViewSingleCapsuleActivity extends AppCompatActivity {

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String postId;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        String capsuleKey = getIntent().getStringExtra("dataKey");


        setContentView(R.layout.activity_view_single_capsule);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewSingleCapsuleActivity.this, ViewCapsulesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://time-capsule-9f74d.appspot.com");
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid()).child("capsules");

        Log.v("Jeannine", databaseReference.child(capsuleKey).toString());
        databaseReference.child(capsuleKey).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Map<String, Object> childData = (Map<String, Object>) task.getResult().getValue();
                    TextView titleText = findViewById(R.id.viewSingleCapsuleTitleText);
                    titleText.setText(childData.get("title").toString());
                    TextView descriptionText = findViewById(R.id.viewSingleCapsuleDescriptionText);
                    descriptionText.setText(childData.get("description").toString());
                }
            }


        });


    }
}
