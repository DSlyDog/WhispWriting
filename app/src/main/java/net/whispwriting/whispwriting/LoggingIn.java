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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LoggingIn extends AppCompatActivity {

    private static final String TAG = "";
    private Toolbar rToolbar;
    private TextInputLayout email;
    private TextInputLayout password;
    private Button lginButton;
    private FirebaseAuth mAuth;
    private ProgressDialog rLogProgress;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggingin);
        rToolbar = (Toolbar) findViewById(R.id.loggingInToolbar);
        setSupportActionBar(rToolbar);
        getSupportActionBar().setTitle("Account Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        rLogProgress = new ProgressDialog(this);


        database = FirebaseDatabase.getInstance().getReference().child("Users");

        email = (TextInputLayout) findViewById(R.id.lginemailInput);
        password = (TextInputLayout) findViewById(R.id.lginpasswdIn);
        lginButton = (Button) findViewById(R.id.logBtn);

        lginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String email_str = email.getEditText().getText().toString();
                String password_str = password.getEditText().getText().toString();
                if (!TextUtils.isEmpty(email_str) || !TextUtils.isEmpty(password_str)){
                    rLogProgress.setTitle("Logging in User");
                    rLogProgress.setCanceledOnTouchOutside(false);
                    rLogProgress.show();
                    loginUser(email_str, password_str);
                }
            }
            });
    }

    public void loginUser(String email, String password){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            rLogProgress.dismiss();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            database.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent(LoggingIn.this, chat.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }else{
                            rLogProgress.hide();
                            Toast.makeText(LoggingIn.this, "Login failed. Check the form and try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}
