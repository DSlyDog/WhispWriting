package net.whispwriting.whispwriting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusUpdate extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout setStatus;
    private Button setStatusButton;
    private DatabaseReference database;
    private FirebaseUser currentUser;
    private ProgressDialog progressDialogue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);
        toolbar = (Toolbar) findViewById(R.id.StatusToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();
        database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        setStatus = (TextInputLayout) findViewById(R.id.setStatus);
        setStatusButton = (Button) findViewById(R.id.setStatusButton);

        String status = getIntent().getStringExtra("status");
        setStatus.getEditText().setText(status);

        progressDialogue = new ProgressDialog(this);

        setStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialogue.setTitle("Saving Changes");
                progressDialogue.setMessage("Please wait");
                progressDialogue.show();
                String status = setStatus.getEditText().getText().toString();
                database.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            progressDialogue.dismiss();
                            Intent accountSettings = new Intent(StatusUpdate.this, AccountSettings.class);
                            startActivity(accountSettings);
                        }else{
                            Toast.makeText(getApplicationContext(), "Saving failed. Check the form and try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });


    }
}