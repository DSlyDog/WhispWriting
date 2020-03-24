package net.whispwriting.whispwriting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class registration extends AppCompatActivity {

    private TextInputLayout username;
    private TextInputLayout email;
    private TextInputLayout password;
    private Button regBtn;
    private FirebaseAuth mAuth;
    private Toolbar rToolbar;
    private ProgressDialog rRegProgress;
    private DatabaseReference cDatabase;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_registration);
        rToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(rToolbar);
        getSupportActionBar().setTitle("Account Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rRegProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        username = (TextInputLayout) findViewById(R.id.unameIn);
        email = (TextInputLayout) findViewById(R.id.emailInput);
        password = (TextInputLayout) findViewById(R.id.passwdIn);
        regBtn = (Button) findViewById(R.id.regBtn);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username_str = username.getEditText().getText().toString();
                String email_str = email.getEditText().getText().toString();
                String password_str = password.getEditText().getText().toString();
                if (!TextUtils.isEmpty(username_str) || !TextUtils.isEmpty(email_str) || !TextUtils.isEmpty(password_str)) {
                    rRegProgress.setTitle("Registering User");
                    rRegProgress.setCanceledOnTouchOutside(false);
                    rRegProgress.show();
                    register_user(username_str, email_str, password_str);
                }

            }
        });
    }

    private void register_user(final String usernameStr, final String emailStr, final String passwordStr) {

    mAuth.createUserWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {

            if (task.isSuccessful()) {
                FirebaseUser curretnUser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = curretnUser.getUid();
                cDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                HashMap<String, String> userMap = new HashMap<>();
                userMap.put("name", usernameStr);
                userMap.put("device_token", deviceToken);
                userMap.put("status", "Hi, I haven't set my status yet.");
                userMap.put("image", "default");
                userMap.put("thumb_image", "default");

                cDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            rRegProgress.dismiss();
                            Intent chatload = new Intent(registration.this, chat.class);
                            startActivity(chatload);
                            finish();
                        }

                    }
                });


            }
            else {
                rRegProgress.hide();
                Toast.makeText(registration.this, "Unable to register. Check the form and try again.", Toast.LENGTH_LONG).show();
            }
        }
    });
    }
}