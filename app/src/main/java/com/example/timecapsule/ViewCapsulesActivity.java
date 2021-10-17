package com.example.timecapsule;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class ViewCapsulesActivity extends AppCompatActivity {

    // help on downloading files:
    // https://stackoverflow.com/questions/39905719/how-to-download-a-file-from-firebase-storage-to-the-external-storage-of-android

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_capsules);
    }
}
