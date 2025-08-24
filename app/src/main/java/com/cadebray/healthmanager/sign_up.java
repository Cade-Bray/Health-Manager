package com.cadebray.healthmanager;

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

public class sign_up extends AppCompatActivity {
    private EditText mEmail;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private UserDao mUserDao;
    private ExecutorService mExecutor;
    private Handler mMainHandler;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign_up_frame), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Define fields
        Button mSignUp = findViewById(R.id.sign_up_confirm_button);
        mEmail = findViewById(R.id.sign_up_email);
        mPassword = findViewById(R.id.sign_up_password);
        mConfirmPassword = findViewById(R.id.sign_up_password_confirm);
        UserDatabase userDatabase = UserDatabase.getDatabase(this);
        mUserDao = userDatabase.userDao();

        // Set up listener
        mSignUp.setOnClickListener(this::onSignUp);

        // Set up executor and handler
        mExecutor = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(getMainLooper());
    }

    /**
     * Called when the sign up button is pressed. This will create a new user. If the user already
     * exists, it will not create a new user. If the passwords do not match, it will not create a
     * new user.
     * @param view The button that was pressed
     */
    public void onSignUp(View view) {
        String password = mPassword.getText().toString();
        String password_confirmed = mConfirmPassword.getText().toString();
        if (password.equals(password_confirmed)) {
            String email = mEmail.getText().toString();
            mExecutor.execute(() -> {
                User user = mUserDao.getUser(email);
                if (user != null) {
                    mMainHandler.post(
                            () -> Toast.makeText(
                                    this,
                                    "User already exists",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }
                else {
                    User newUser = new User(password, email, true);
                    mUserDao.insert(newUser);
                    mMainHandler.post(
                        () -> Toast.makeText(
                                this,
                                "User created",
                                Toast.LENGTH_SHORT
                        ).show()
                    );
                }
            });
            finish();
        } else {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            mPassword.setText("");
            mConfirmPassword.setText("");
        }
    }
}
