package com.example.timecapsule;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CreateCapsuleActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "0";
    FirebaseDatabase database;
    DatabaseReference myRef;

    Calendar calendar;
    ImageView imgView;
    TextView txtDate, txtTime;
    EditText txtTitle, txtDesc;
    private int year, month, day, hour, minute;

    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_capsule);

        txtDate=(TextView) findViewById(R.id.dateText);
        txtTime=(TextView) findViewById(R.id.timeText);

        txtTitle = (EditText) findViewById(R.id.titleText);
        txtDesc = (EditText) findViewById(R.id.descriptionText);

        imgView = (ImageView) findViewById(R.id.image_view);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        String userId = UserId.getUid();

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imgView.setImageBitmap(imageBitmap);
                }
            }
        });



        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users").child(userId).child("capsules");

        createNotificationChannel();


    }


    public void addCapsule(View v) {
        DatabaseReference newCapsuleRef = myRef.push();
        newCapsuleRef.child("title").setValue(txtTitle.getText().toString());
        newCapsuleRef.child("description").setValue(txtDesc.getText().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        newCapsuleRef.child("opendatetime").setValue(sdf.format(calendar.getTime()));

        // set up notification
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

    public void showDatePickerDialog(View v) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR , year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH , dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                        txtDate.setText(sdf.format(calendar.getTime()));

                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    public void showTimePickerDialog(View v) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 1);

                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                        txtTime.setText(sdf.format(calendar.getTime()));
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    public void takePicture(View v) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            activityResultLauncher.launch(intent);
        } else {
            Toast.makeText(CreateCapsuleActivity.this, "There is no camera available",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void addRecording(View v) {

        Intent intent = new Intent(CreateCapsuleActivity.this, RecordActivity.class);
        startActivity(intent);

    }


}