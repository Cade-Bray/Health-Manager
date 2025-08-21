package com.cadebray.healthmanager;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MainActivity extends AppCompatActivity {

    private UserContentDatabase mUserContentDatabase;
    private UserContentDatabase.WeightLog[] mWeights;
    private Button mLogWeightButton;
    private GridLayout mWeightsGrid;
    private static final int LOG_WEIGHT_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Define elements
        mUserContentDatabase = new UserContentDatabase(this);
        mLogWeightButton = findViewById(R.id.log_weight_button);
        mWeightsGrid = findViewById(R.id.weights_grid);

        // Gather weights from database
        loadAndDisplayWeights();

        // Set Log weight listener
        mLogWeightButton.setOnClickListener(this::onLogWeight);
    }

    /**
     * Loads and displays the weights from the database. This function handles the logic for
     * creating textviews and buttons for each weight.
     */
    private void loadAndDisplayWeights() {
        int childCount = mWeightsGrid.getChildCount();
        int COLUMN_COUNT = 3;
        if (childCount > COLUMN_COUNT){
            mWeightsGrid.removeViews(COLUMN_COUNT, childCount - COLUMN_COUNT);
        }

        // Gather the weights from the database and setup
        mWeights = mUserContentDatabase.getWeights();

        // There are no weights to display
        if (mWeights == null || mWeights.length == 0) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            TextView emptyTextView = new TextView(this);
            params.rowSpec = GridLayout.spec(1); // Starting after the header row
            params.columnSpec = GridLayout.spec(0, COLUMN_COUNT); // Span all columns
            params.setGravity(Gravity.CENTER_HORIZONTAL);
            emptyTextView.setLayoutParams(params);
            emptyTextView.setText(R.string.no_weights);
            emptyTextView.setTextSize(20);
            emptyTextView.setPadding(10, 10, 10, 10);
            mWeightsGrid.addView(emptyTextView);
            return;
        }

        // There are weights to display, create the textviews and buttons
        for (int i = 0; i < mWeights.length; i++) {
            UserContentDatabase.WeightLog log = mWeights[i];

            // Create a textview for weight
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            TextView weightTextView = new TextView(this);
            String weight = log.weight + " " + log.weightUnit;
            weightTextView.setText(weight);
            weightTextView.setTextSize(15);
            weightTextView.setPadding(10, 10, 10, 10);
            params.width = WRAP_CONTENT;
            params.rowSpec = GridLayout.spec(i);
            params.columnSpec = GridLayout.spec(0);
            params.setGravity(Gravity.CENTER_HORIZONTAL);
            weightTextView.setLayoutParams(params);
            mWeightsGrid.addView(weightTextView);

            // Create a textview for date
            params = new GridLayout.LayoutParams();
            int marginEndDp = 25;
            float density = getResources().getDisplayMetrics().density; // Get screen density
            int marginEndPixels = (int) (marginEndDp * density);
            params.setMarginEnd(marginEndPixels);
            params.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView dateTextView = new TextView(this);
            try {
                LocalDateTime dateTime = log.timestamp;

                // Format the date
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                String formattedDate = dateTime.format(outputFormatter);
                dateTextView.setText(formattedDate);

            } catch (DateTimeParseException e) {
                // Handle the case where the timestamp string is not in the expected format
                Log.e("MainActivity", "Error parsing date: " + e.getMessage());
                dateTextView.setText(R.string.invalid_date);
            } catch (NullPointerException e) {
                // Handle the case where log or log.timestamp is null
                Log.e("MainActivity", "Error parsing date: " + e.getMessage());
                dateTextView.setText(R.string.invalid_date);
            }
            dateTextView.setTextSize(15);
            dateTextView.setPadding(10, 10, 10, 10);
            params.width = WRAP_CONTENT;
            params.rowSpec = GridLayout.spec(i);
            params.columnSpec = GridLayout.spec(1);
            dateTextView.setLayoutParams(params);
            mWeightsGrid.addView(dateTextView);

            // Create a button to delete
            params = new GridLayout.LayoutParams();
            params.setGravity(Gravity.CENTER_HORIZONTAL);
            Context context = mWeightsGrid.getContext();
            Button deleteButton = new com.google.android.material.button.MaterialButton(
                    context,
                    null
            );
            deleteButton.setText(R.string.remove);
            deleteButton.setId(log.id);
            deleteButton.setTextSize(15);
            deleteButton.setPadding(10, 10, 10, 10);
            params.setMarginEnd(marginEndPixels);
            params.width = WRAP_CONTENT;
            params.rowSpec = GridLayout.spec(i);
            params.columnSpec = GridLayout.spec(2);
            deleteButton.setLayoutParams(params);
            // Set the button's click listener
            deleteButton.setOnClickListener(this::onRemove);
            mWeightsGrid.addView(deleteButton);
        }
    }

    /**
     * Called when the remove weight button is pressed
     * @param view The button that was pressed
     */
    public void onRemove(View view){
        boolean status = mUserContentDatabase.removeLoggedWeight(view.getId());
        if (!status) {
            Toast.makeText(this, "Failed to remove weight", Toast.LENGTH_SHORT).show();
        }
        mWeightsGrid.removeAllViews();
        loadAndDisplayWeights();
    }

    /**
     * Called when the log weight button is pressed
     * @param view The button that was pressed
     */
    public void onLogWeight(View view){
        Intent intent = new Intent(this, log_weight.class);
        startActivityForResult(intent, LOG_WEIGHT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOG_WEIGHT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Reload and display the weights
                Log.d("MainActivity", "Returned from log_weight successfully. Refreshing weights.");
                mWeightsGrid.removeAllViews();
                loadAndDisplayWeights();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("MainActivity", "Returned from log_weight with cancel.");
            }
        }
    }
}