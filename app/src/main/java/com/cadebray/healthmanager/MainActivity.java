package com.cadebray.healthmanager;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private List<com.cadebray.healthmanager.Log> mWeights;
    private LogDao mLogDao;
    private ExecutorService mExecutor;
    private Handler mMainHandler;
    private String mEmail;
    private String mPhone;
    private GridLayout mWeightsGrid;
    private String mGoal;
    private String mUnits;
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
        LogDatabase mLogDatabase = LogDatabase.getDatabase(this);
        mLogDao = mLogDatabase.logDao();
        mExecutor = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(getMainLooper());
        Button mLogWeightButton = findViewById(R.id.log_weight_button);
        mWeightsGrid = findViewById(R.id.weights_grid);

        // Get the email from the intent
        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");
        mGoal = intent.getStringExtra("goal");
        mUnits = intent.getStringExtra("units");
        mPhone = intent.getStringExtra("phone");

        // Gather weights from database
        loadWeights();

        // Set Log weight listener
        mLogWeightButton.setOnClickListener(this::onLogWeight);
    }

    /**
     * This method loads the weights from the database and displays them.
     */
    private void loadWeights(){
        mExecutor.execute(() -> {
            mWeights = mLogDao.getLogs(mEmail);
            mMainHandler.post(this::displayWeights);
        });
    }

    /**
     * This method displays the weights in the grid layout. If there are no weights, it displays a
     * message. If there are weights, it displays them. Ideally you should call loadWeights() first
     * which will call this method. If this is called by itself it will only display the weights
     * that are currently assigned to memory. If you want to refresh the weights to be updated and
     * displayed you should call loadWeights.
     */
    private void displayWeights() {
        int childCount = mWeightsGrid.getChildCount();
        int COLUMN_COUNT = 3;
        if (childCount > COLUMN_COUNT){
            mWeightsGrid.removeViews(COLUMN_COUNT, childCount - COLUMN_COUNT);
        }

        // There are no weights to display
        if (mWeights == null || mWeights.isEmpty()) {
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

        // There are weights to display, create the text views and buttons
        for (int i = 0; i < mWeights.size(); i++) {
            //UserContentDatabase.WeightLog log = mWeights[i]; Depreciated
            com.cadebray.healthmanager.Log log = mWeights.get(i);

            // Create a textview for weight
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            TextView weightTextView = new TextView(this);
            String weight = log.getWeight() + " " + log.getWeightUnit();
            // Set the weight here
            weightTextView.setText(weight);
            // Set the id here
            weightTextView.setId((int) log.getId());
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
            // Set the date here
            dateTextView.setText(log.getDate());
            // Set the id here
            dateTextView.setId((int) log.getId());
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
            deleteButton.setId((int) log.getId());
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

            // Set the Weight and Date click listeners here
            weightTextView.setOnClickListener(this::onUpdate);
            dateTextView.setOnClickListener(this::onUpdate);
        }
    }

    /**
     * Called when the remove weight button is pressed
     * @param view The button that was pressed
     */
    public void onRemove(View view){
        // Calling the remove method from the database which needs to be called on a background thread.
        mExecutor.execute(() -> {
            mLogDao.deleteLog(view.getId(), mEmail);
            if (mLogDao.getLog(view.getId(), mEmail) != null) {
                // Toast failed to remove weight called on the main thread because toasts are not
                // allowed on background threads
                mMainHandler.post(() -> Toast.makeText(
                        MainActivity.this,
                        "Failed to remove weight",
                        Toast.LENGTH_LONG
                ).show());
            }
        });

        // Call the method to refresh the weights
        mWeightsGrid.removeAllViews();
        loadWeights();
    }

    /**
     * Called when the log weight button is pressed
     * @param view The button that was pressed
     */
    public void onLogWeight(View view){
        Intent intent = new Intent(this, log_weight.class);
        intent.putExtra("email", mEmail);
        intent.putExtra("goal", mGoal);
        intent.putExtra("units", mUnits);
        intent.putExtra("phone", mPhone);
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
                loadWeights();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("MainActivity", "Returned from log_weight with cancel.");
            }
        }
    }

    public void onUpdate(View view){
        Intent intent = new Intent(this, log_weight.class);
        intent.putExtra("id", view.getId());
        intent.putExtra("email", mEmail);
        intent.putExtra("goal", mGoal);
        intent.putExtra("units", mUnits);
        intent.putExtra("phone", mPhone);
        startActivityForResult(intent, LOG_WEIGHT_REQUEST_CODE);
    }
}