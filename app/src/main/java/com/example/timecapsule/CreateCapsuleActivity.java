package com.example.timecapsule;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateCapsuleActivity extends AppCompatActivity implements AddMediaDialogFragment.NoticeDialogListener{

    private static final String TAG = CreateCapsuleActivity.class.getSimpleName();
    private static final String CHANNEL_ID = "0";

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String postId;

    private ImageButton takePictureImageButton;
    private TextView dateTextView;
    private TextView timeTextView;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private FloatingActionButton addCapsuleFloatingActionButton;

    private Calendar calendar;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private ActivityResultLauncher<Intent> takePictureActivityResultLauncher;
    private ActivityResultLauncher<Intent> takeVideoActivityResultLauncher;
    private ActivityResultLauncher<String> selectPictureActivityResultLauncher;
    private ActivityResultLauncher<String> selectVideoActivityResultLauncher;
    private Uri currentPhotoUri;
    private Uri currentVideoUri;

    private boolean imageUploaded;
    private boolean videoUploaded;
    public static boolean recordingUploaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_capsule);

        imageUploaded = false;
        videoUploaded = false;
        recordingUploaded = false;

        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://time-capsule-9f74d.appspot.com");
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid()).child("capsules");

        takePictureImageButton = (ImageButton) findViewById(R.id.activity_create_capsule_ibtn_take_picture);
        dateTextView = (TextView) findViewById(R.id.activity_create_capsule_tv_choose_date);
        timeTextView = (TextView) findViewById(R.id.activity_create_capsule_tv_choose_time);
        titleEditText = (EditText) findViewById(R.id.activity_create_capsule_et_title);
        descriptionEditText = (EditText) findViewById(R.id.activity_create_capsule_et_description);
        addCapsuleFloatingActionButton = (FloatingActionButton) findViewById(R.id.activity_create_capsule_fab_save);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        takePictureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new AddMediaDialogFragment();
                newFragment.show(getSupportFragmentManager(), "media upload options");
            }
        });

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        addCapsuleFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndAddCapsule();
                Intent intent = new Intent(CreateCapsuleActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        takePictureActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUploaded = true;
                }
            }
        });

        takeVideoActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    videoUploaded = true;
                    currentVideoUri = result.getData().getData();
                }
            }
        });

        selectPictureActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>(){
            @Override
            public void onActivityResult(Uri result) {
                currentPhotoUri = result;
                imageUploaded = true;
            }
        });

        selectVideoActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>(){
            @Override
            public void onActivityResult(Uri result) {
                currentVideoUri = result;
                videoUploaded = true;
            }
        });

        createNotificationChannel();
    }

    private void saveAndAddCapsule() {
        DatabaseReference newCapsuleRef = databaseReference.push();
        // Get the unique ID generated by a push()
        postId = newCapsuleRef.getKey();
        newCapsuleRef.child("title").setValue(titleEditText.getText().toString());
        newCapsuleRef.child("description").setValue(descriptionEditText.getText().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        newCapsuleRef.child("opendatetime").setValue(sdf.format(calendar.getTime()));

        setUpNotification();
        if (recordingUploaded){
            uploadAudio();
        }
        if (imageUploaded) {
            uploadImage();
        }
        if (videoUploaded) {
            uploadVideo();
        }
    }

    private void uploadAudio() {
        // TODO: change uuid.random to the unique ID of the capsule and set it as the child
        StorageReference filePath = storageReference.child("audio").child(auth.getCurrentUser().getUid().toString()).child(postId);
        Uri uri = Uri.fromFile(new File(RecordActivity.fileName));

        filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "audio upload successful");
                }
                else {
                    Log.e(TAG, "audio upload unsuccessful");
                }
            }
        });
    }

    private void setUpNotification() {
        Context context = this.getApplicationContext();
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, (int)System.currentTimeMillis(), intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR , year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH , dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                        dateTextView.setText(sdf.format(calendar.getTime()));
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 1);

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                        timeTextView.setText(sdf.format(calendar.getTime()));
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(CreateCapsuleActivity.this, "Photo was not saved",
                        Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.timecapsule.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureActivityResultLauncher.launch(takePictureIntent);
            }
        }
    }

    private void takeVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            takeVideoActivityResultLauncher.launch(takeVideoIntent);
        }
    }

    private void selectPicture() {
        selectPictureActivityResultLauncher.launch("image/*");
    }

    private void selectVideo() {
        selectVideoActivityResultLauncher.launch("video/*");
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // create a better image name?
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoUri = Uri.fromFile(new File(image.getAbsolutePath()));
        return image;
    }

    private void addRecording() {
        Intent intent = new Intent(CreateCapsuleActivity.this, RecordActivity.class);
        startActivity(intent);
    }

    private void uploadImage() {
        StorageReference filePath = storageReference.child("images").child(auth.getCurrentUser().getUid().toString()).child(postId);

        filePath.putFile(currentPhotoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "image upload successful");
                }
                else {
                    Log.e(TAG, "image upload unsuccessful");
                }
            }
        });
    }

    private void uploadVideo() {
        StorageReference filePath = storageReference.child("videos").child(auth.getCurrentUser().getUid().toString()).child(postId);

        filePath.putFile(currentVideoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "video upload successful");
                }
                else {
                    Log.e(TAG, "video upload unsuccessful");
                }
            }
        });
    }

    @Override
    public void onTakePictureClick(DialogFragment dialog) {
        takePicture();
    }

    @Override
    public void onChoosePictureClick(DialogFragment dialog) {
        selectPicture();
    }

    @Override
    public void onTakeVideoClick(DialogFragment dialog) {
        takeVideo();
    }

    @Override
    public void onChooseVideoClick(DialogFragment dialog) {
        selectVideo();
    }

    @Override
    public void onUploadAudioClick(DialogFragment dialog) {
        addRecording();
    }
}