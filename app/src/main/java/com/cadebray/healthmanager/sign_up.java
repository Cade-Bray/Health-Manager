package com.cadebray.healthmanager;

import android.graphics.Insets;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class sign_up extends AppCompatActivity {
    private Button mSignUp;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private _UserDatabase mUserDatabase;

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
        mSignUp = findViewById(R.id.sign_up_confirm_button);
        mEmail = findViewById(R.id.sign_up_email);
        mPassword = findViewById(R.id.sign_up_password);
        mConfirmPassword = findViewById(R.id.sign_up_password_confirm);
        mUserDatabase = new _UserDatabase(this);

        // Set up listener
        mSignUp.setOnClickListener(this::onSignUp);
    }

    public void onSignUp(View view) {
        String password = mPassword.getText().toString();
        String password_confirmed = mConfirmPassword.getText().toString();
        if (password.equals(password_confirmed)) {
            String email = mEmail.getText().toString();
            long userId = mUserDatabase.addUser(email, password, true);
            if (userId != -1){
                Toast.makeText(this, "Signed up Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            mPassword.setText("");
            mConfirmPassword.setText("");
        }
    }
}
