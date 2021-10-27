package com.example.timecapsule;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class RecordActivity extends AppCompatActivity {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private FirebaseAuth auth;
    private StorageReference storageReference;

    private static String fileName;

    private FloatingActionButton recordButton;
    private FloatingActionButton playButton;
    private Button uploadRecordingButton;

    private MediaRecorder recorder;
    private MediaPlayer player;

    private boolean mStartRecording = true;
    private boolean mStartPlaying = true;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://time-capsule-9f74d.appspot.com");

        recordButton = (FloatingActionButton) findViewById(R.id.activity_record_btn_record);
        playButton = (FloatingActionButton) findViewById(R.id.activity_record_btn_play);
        uploadRecordingButton = (Button) findViewById(R.id.activity_record_btn_upload_recording);

        // TODO: find better storage location
        // Record to the external cache directory for visibility
        setFileName(getExternalCacheDir().getAbsolutePath());
        setFileName(getFileName() + "/audioFile");

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    recordButton.setImageResource(R.drawable.ic_stop); //stop recording
                    recordButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
                } else {
                    recordButton.setImageResource(R.drawable.ic_mic);    // start recording
                    recordButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.red)));
                }
                mStartRecording = !mStartRecording;

            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    playButton.setImageResource(R.drawable.ic_stop); //stop playing
                } else {
                    playButton.setImageResource(R.drawable.ic_play);  //start playing
                }
                mStartPlaying = !mStartPlaying;

            }
        });

        uploadRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //uploadAudio();
                CreateCapsuleActivity.setRecordingUploaded(true);
                Toast.makeText(RecordActivity.this, "Recording uploaded", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) {
            finish();
        }

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            Log.i(TAG, "preparing to play");
            player.setDataSource(getFileName());
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(getFileName());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String fileName) {
        RecordActivity.fileName = fileName;
    }
}