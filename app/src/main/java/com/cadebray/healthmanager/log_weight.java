package com.cadebray.healthmanager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class log_weight extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private LogDao mLogDao;
    private String mGoal;
    private String mGoalUnits;
    private EditText mWeight;
    private RadioGroup mUnits;
    private DatePicker mDate;
    private TimePicker mTime;
    private String mEmail;
    private String mPhone;
    private ExecutorService mExecutor;
    private Handler mMainHandler;
    private boolean mUpdate = false;
    private static long mUpdateId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_weight);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set Log weight listener
        Button mLogWeightButton = findViewById(R.id.submit);
        mLogWeightButton.setOnClickListener(this::onLogWeight);

        // Initialize the database
        mExecutor = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(getMainLooper());
        LogDatabase mLogDatabase = LogDatabase.getDatabase(this);
        mLogDao = mLogDatabase.logDao();

        // Initialize the views
        mWeight = findViewById(R.id.weight);
        mUnits = findViewById(R.id.units_radio);
        mDate = findViewById(R.id.date_picker);
        mTime = findViewById(R.id.time_picker);

        // Get the email from the intent
        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");

        // Get the goal from the intent
        mGoal = intent.getStringExtra("goal");
        mGoalUnits = intent.getStringExtra("units");

        // Set the phone number
        mPhone = intent.getStringExtra("phone");

        // Check to see if extras are present for this being an update for an id field
        int id = intent.getIntExtra("id", -1);
        if (id != -1) {
            // Set the fields to the values of the log
            mExecutor.execute(() -> {
                // Get the log from the database
                Log log = mLogDao.getLog(id, mEmail);

                // Set the weight
                mWeight.setText(String.valueOf(log.getWeight()));

                // Set the units
                if (log.getWeightUnit().equals("lbs")) {
                    mUnits.check(R.id.lb);
                } else {
                    mUnits.check(R.id.kg);
                }

                // Set the date and time
                String[] dateParts = log.getDate().split("-");
                String[] timeParts = log.getTime().split(":");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1;
                int day = Integer.parseInt(dateParts[2]);
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                mDate.updateDate(year, month, day);
                mTime.setHour(hour);
                mTime.setMinute(minute);
                mUpdateId = id;
            });
            mUpdate = true;
        }
    }

    public void onLogWeight(View view) {
        final Log log = new Log();

        // check the goal
        checkGoal(Float.parseFloat(mWeight.getText().toString()));

        // Set the username
        log.setUsername(mEmail);

        // Get the weight from the EditText
        String weightString = mWeight.getText().toString();
        if (!weightString.isEmpty()) {
            log.setWeight(Float.parseFloat(weightString));
        } else {
            Toast.makeText(this, "Please provide a valid weight", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the units from the RadioGroup
        int selectedId = mUnits.getCheckedRadioButtonId();
        if (selectedId == R.id.lb) {
            log.setWeightUnit("lbs");
        } else if (selectedId == R.id.kg) {
            log.setWeightUnit("kg");
        } else {
            Toast.makeText(this, "Please select a unit", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the date from the DatePicker
        int year = mDate.getYear();
        int month = mDate.getMonth();
        int day = mDate.getDayOfMonth();

        // Get the time from the TimePicker
        int hour = mTime.getHour();
        int minute = mTime.getMinute();

        // If this is an update, set the id
        if (mUpdate) {
            log.setId(mUpdateId);
        }

        // Set the timestamp
        LocalDateTime timestamp = LocalDateTime.of(year, month + 1, day, hour, minute);

        // Set the log's Date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        log.setDate(timestamp.format(formatter));

        // Set the log's time
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");
        log.setTime(timestamp.format(formatter2));

        // Set to the background thread
        mExecutor.execute(() -> {
            long rowId;
            boolean success = false;
            try {
                if (mUpdate) {
                    mLogDao.update(log);
                    android.util.Log.i("LogWeightActivity", "Updated weight");
                    success = true;
                } else {
                    rowId = mLogDao.insert(log);
                    android.util.Log.i("LogWeightActivity", "Logged weight");
                    success = rowId > -1;
                }
            } catch (Exception e) {
                android.util.Log.e("LogWeightActivity", "Error logging weight");
            }

            final boolean finalSuccess = success;

            mMainHandler.post(
                    () -> {
                        if (finalSuccess) {
                            // Successful logging
                            Toast.makeText(
                                    log_weight.this,
                                    "Weight logged",
                                    Toast.LENGTH_SHORT
                            ).show();
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                        } else {
                            // Failed logging
                            Intent resultIntent = new Intent();
                            setResult(RESULT_CANCELED, resultIntent);
                        }
                        finish();
                    }
            );
        });
    }

    /**
     * Checks if value is the current goal. If it is send a notification congratulating them.
     */
    private void checkGoal(float weight) {
        if (weight >= Float.parseFloat(mGoal)) {
            if (checkSelfPermission(android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d("SMS_DEBUG", "Permission NOT granted. Requesting...");
                requestPermissions(new String[]{android.Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            } else {
                android.util.Log.d("SMS_DEBUG", "Permission ALREADY granted. Proceeding to send.");
                sendSmsMessage();
            }
        }
    }

    private void sendSmsMessage(){
        if (mPhone != null) {
            android.util.Log.d("SMS_DEBUG", "sendSmsMessage - mPhone: " + mPhone);
            try {
                SmsManager smsManager = android.telephony.SmsManager.getDefault();
                smsManager.sendTextMessage(
                        mPhone,
                        null,
                        "You have reached your goal! Your current goal is " + mGoal + " " +
                                mGoalUnits + "!",
                        null,
                        null
                );
                Toast.makeText(this, "Goal Achieved, Congratulations!", Toast.LENGTH_SHORT).show();
                android.util.Log.i("LogWeightActivity", "SMS sent by sendSmsMessage");
            } catch (Exception e) {
                android.util.Log.e("SMS_DEBUG", "Error sending SMS", e);
                Toast.makeText(this, "Failed to send SMS.", Toast.LENGTH_SHORT).show();
            }
        } else {
            android.util.Log.w("SMS_DEBUG", "sendSmsMessage - mPhone is NULL.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d("SMS_DEBUG", "Permission GRANTED by user via dialog.");
                sendSmsMessage();
            } else {
                android.util.Log.d("SMS_DEBUG", "Permission DENIED by user via dialog.");
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
