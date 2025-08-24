package com.cadebray.healthmanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class log_weight extends AppCompatActivity {

    private LogDao mLogDao;
    private LogDatabase mLogDatabase;
    private EditText mWeight;
    private RadioGroup mUnits;
    private DatePicker mDate;
    private TimePicker mTime;
    private String mEmail;
    private ExecutorService mExecutor;
    private Handler mMainHandler;

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
        mLogDatabase = LogDatabase.getDatabase(this);
        mLogDao = mLogDatabase.logDao();

        // Initialize the views
        mWeight = findViewById(R.id.weight);
        mUnits = findViewById(R.id.units_radio);
        mDate = findViewById(R.id.date_picker);
        mTime = findViewById(R.id.time_picker);

        // Get the email from the intent
        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");
    }

    public void onLogWeight(View view) {
        final Log log = new Log();

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

        // Set the timestamp
        LocalDateTime timestamp = LocalDateTime.of(year, month, day, hour, minute);

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
                rowId = mLogDao.insert(log);
                android.util.Log.i("LogWeightActivity", "Logged weight");
                success = rowId > -1;
            } catch (Exception e) {
                android.util.Log.e("LogWeightActivity", "Error logging weight");
            }

            final boolean finalSuccess = success;

            mMainHandler.post(
                    () -> {
                        if (finalSuccess) {
                            Toast.makeText(log_weight.this, "Weight logged", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                        } else {
                            Intent resultIntent = new Intent();
                            setResult(RESULT_CANCELED, resultIntent);
                        }
                        finish();
                    }
            );
        });
    }

    /**
     * Checks if value is the current goal. If it is send a notification congratulating them
     * @param value The value to check
     * @param goal The goal to check against
     */
    private void checkGoal(float value, float goal) {
        if (value >= goal) {
            // Send a notification to the user Push notification

        }
    }
}
