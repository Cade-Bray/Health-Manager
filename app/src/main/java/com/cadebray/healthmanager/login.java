package com.cadebray.healthmanager;

import android.content.Intent;
import android.graphics.Insets;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class login extends AppCompatActivity {
    private EditText mEmail;
    private EditText mPassword;
    private UserDao mUserDao;
    private ExecutorService mExecutor;
    private Handler mMainHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Define Executor and Handler
        mExecutor = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(getMainLooper());

        // Define Editable Fields
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);

        // Define user database
        //mUserUserDatabase = new _UserDatabase(this);
        UserDatabase mUserDatabase = UserDatabase.getDatabase(this);
        mUserDao = mUserDatabase.userDao();

        // Set the forgot button listener
        Button forgot_button = findViewById(R.id.Forgot_button);
        forgot_button.setOnClickListener(this::onForgotPassword);

        // Set the sign in button listener
        Button login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(this::onSignIn);

        // Set the sign up listener
        Button sign_up_button = findViewById(R.id.sign_up);
        sign_up_button.setOnClickListener(this::onSignUp);

        // Set Demo Listener
        login_button.setOnLongClickListener(this::onLongSignIn);
    }

    /**
     * Called when the forgot password button is pressed
     * @param view The button that was pressed
     */
    public void onForgotPassword(View view){
        Intent intent = new Intent(this, forgot_password.class);
        startActivity(intent);
    }


    /**
     * Called when the sign in button is pressed
     * @param view The button that was pressed
     */
    public void onSignIn(View view){
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {
            // Authenticate the user
            mExecutor.execute(() -> {
                User user = mUserDao.authenticateUser(email, password);
                if (user != null) {
                    mMainHandler.post(() -> {
                        Intent intent = new Intent(login.this, MainActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        Toast.makeText(
                                login.this,
                                "Successfully Authenticated!",
                                Toast.LENGTH_LONG
                        ).show();
                    });
                } else {
                    mMainHandler.post(this::failedToAuth);
                }
            });

        } else {
            failedToAuth();
        }
    }

    /**
     * This is called when the user fails to authenticate. Basically clean up and notification.
     */
    private void failedToAuth(){
        Toast.makeText(
                login.this,
                "Failed to Authenticate...",
                Toast.LENGTH_LONG
        ).show();
        mPassword.setText("");
    }

    /**
     * Called when the sign in button is long pressed
     * @param view The button that was pressed
     * @return Whether the long press was successful
     */
    public boolean onLongSignIn(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        Toast.makeText(
                login.this,
                "Demo mode activated.",
                Toast.LENGTH_LONG
        ).show();
        return true;
    }

    /**
     * Called when the sign up button is pressed
     * @param view The button that was pressed
     */
    public void onSignUp(View view){
        Intent intent = new Intent(this, sign_up.class);
        startActivity(intent);
    }
}
