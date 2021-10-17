package com.example.timecapsule;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateCapsuleActivity extends AppCompatActivity {

    private static final String TAG = CreateCapsuleActivity.class.getSimpleName();
    private static final String CHANNEL_ID = "0";

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    // not necessary. Will remove later
    private ImageView imgView;

    private ImageButton takePictureImageButton;
    private TextView dateTextView;
    private TextView timeTextView;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private FloatingActionButton recordFloatingActionButton;
    private FloatingActionButton addCapsuleFloatingActionButton;

    private Calendar calendar;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_capsule);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(auth.getCurrentUser().getUid()).child("capsules");

        takePictureImageButton = (ImageButton) findViewById(R.id.activity_create_capsule_ibtn_take_picture);
        dateTextView = (TextView) findViewById(R.id.activity_create_capsule_tv_choose_date);
        timeTextView = (TextView) findViewById(R.id.activity_create_capsule_tv_choose_time);
        titleEditText = (EditText) findViewById(R.id.activity_create_capsule_et_title);
        descriptionEditText = (EditText) findViewById(R.id.activity_create_capsule_et_description);
        recordFloatingActionButton = (FloatingActionButton) findViewById(R.id.activity_create_capsule_fab_record);
        addCapsuleFloatingActionButton = (FloatingActionButton) findViewById(R.id.activity_create_capsule_fab_save);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        // not needed. Will delete later
        imgView = (ImageView) findViewById(R.id.image_view);

        takePictureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
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

        recordFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRecording();
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

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    setImageViewPicture();
                }
            }
        });
        createNotificationChannel();
    }

    private void saveAndAddCapsule() {
        DatabaseReference newCapsuleRef = databaseReference.push();
        newCapsuleRef.child("title").setValue(titleEditText.getText().toString());
        newCapsuleRef.child("description").setValue(descriptionEditText.getText().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        newCapsuleRef.child("opendatetime").setValue(sdf.format(calendar.getTime()));

        setUpNotification();
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

        // wtf :'( I had to comment out that method to make it work

        //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Log.i(TAG, "in first");
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
                activityResultLauncher.launch(takePictureIntent);
            }
        //}
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setImageViewPicture() {
        // Get the dimensions of the View
        int targetW = imgView.getWidth();
        int targetH = imgView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imgView.setImageBitmap(bitmap);
    }

    private void addRecording() {
        Intent intent = new Intent(CreateCapsuleActivity.this, RecordActivity.class);
        startActivity(intent);
    }
}