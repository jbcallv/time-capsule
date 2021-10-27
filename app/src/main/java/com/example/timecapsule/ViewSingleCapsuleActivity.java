package com.example.timecapsule;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class ViewSingleCapsuleActivity extends AppCompatActivity {

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private Button backButton;

    private MediaPlayer player;

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

        //Gets Description Text
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

        //Gets static image
        try {
            storageReference.child("images").child(auth.getCurrentUser().getUid()).child(capsuleKey).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    byte[] bytes = (byte[]) o;
                    ImageView capsuleImage = findViewById(R.id.viewSingleCapsuleImage);
                    capsuleImage.setVisibility(View.VISIBLE);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    capsuleImage.setImageBitmap(bmp);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
        catch (Exception e){

        }

        //Gets video image
        if(storageReference.child("videos").child(auth.getCurrentUser().getUid()).child(capsuleKey) != null) {

            storageReference.child("videos").child(auth.getCurrentUser().getUid()).child(capsuleKey).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    VideoView capsuleVideo = findViewById(R.id.viewSingleCapsuleVideo);
                    capsuleVideo.setVideoURI(uri);
                    capsuleVideo.setVisibility(View.VISIBLE);
                    capsuleVideo.requestFocus();
                    capsuleVideo.start();
                    MediaController ctlr = new MediaController(ViewSingleCapsuleActivity.this);
                    capsuleVideo.setMediaController(ctlr);
                }
            });

        }

        //Gets audio file
        storageReference.child("audio").child(auth.getCurrentUser().getUid()).child(capsuleKey).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                player = new MediaPlayer();
                try {
                    player.setDataSource(uri.toString());
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    Log.e("audio", "prepare() failed");
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onBackPressed(){
        //Go back to MainActivity
        Intent intent = new Intent(ViewSingleCapsuleActivity.this, ViewCapsulesActivity.class);
        startActivity(intent);
        finish();
    }
}
