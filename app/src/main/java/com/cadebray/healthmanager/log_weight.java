package com.cadebray.healthmanager;

import android.content.Intent;
import android.os.Bundle;
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

public class log_weight extends AppCompatActivity {

    private UserContentDatabase mUserContentDatabase;
    private EditText mWeight;
    private RadioGroup mUnits;
    private DatePicker mDate;
    private TimePicker mTime;

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
        mUserContentDatabase = new UserContentDatabase(this);

        // Initialize the views
        mWeight = findViewById(R.id.weight);
        mUnits = findViewById(R.id.units_radio);
        mDate = findViewById(R.id.date_picker);
        mTime = findViewById(R.id.time_picker);
    }

    public void onLogWeight(View view) {
        UserContentDatabase.WeightLog log = new UserContentDatabase.WeightLog();

        // Get the weight from the EditText
        String weightString = mWeight.getText().toString();
        if (!weightString.isEmpty()) {
            log.weight = Float.parseFloat(weightString);
        } else {
            Toast.makeText(this, "Please provide a valid weight", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the units from the RadioGroup
        int selectedId = mUnits.getCheckedRadioButtonId();
        if (selectedId == R.id.lb) {
            log.weightUnit = "lbs";
        } else if (selectedId == R.id.kg) {
            log.weightUnit = "kg";
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

        // Set the log's timestamp
        log.timestamp = LocalDateTime.of(year, month, day, hour, minute);

        long success = mUserContentDatabase.logWeight(log);

        if (success == -1) {
            Toast.makeText(this, "Failed to log weight", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            setResult(RESULT_CANCELED, resultIntent);
        } else {
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
        }

        Toast.makeText(this, "Weight logged", Toast.LENGTH_SHORT).show();
        finish();
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
