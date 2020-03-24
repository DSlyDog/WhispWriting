package net.whispwriting.whispwriting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettings extends AppCompatActivity {

    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;
    private CircleImageView pImage;
    private TextView uName;
    private TextView uStatus;
    private Button statusButton;
    private Button userButton;
    private Button imageButton;
    private StorageReference profileImageStorage;
    private ProgressDialog progressDialog;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        Toolbar rToolbar = (Toolbar) findViewById(R.id.settingsToolbar);
        setSupportActionBar(rToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        uName = (TextView) findViewById(R.id.DisplayNameText);
        pImage = (CircleImageView) findViewById(R.id.profile_image);
        uStatus = (TextView) findViewById(R.id.statusText);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = currentUser.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        userDatabase.keepSynced(true);

        profileImageStorage = FirebaseStorage.getInstance().getReference();

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                if (name.length() >= 22){
                    uName.setTextSize(22);
                }

                uName.setText(name);
                uStatus.setText(status);
                if (!image.equals("default")) {
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .into(pImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    // do nohthing
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(image).into(pImage);
                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        statusButton = (Button) findViewById(R.id.statusButton);

        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = uStatus.getText().toString();
                Intent statusUpdate = new Intent(AccountSettings.this, StatusUpdate.class);
                statusUpdate.putExtra("status", status);
                startActivity(statusUpdate);
            }
        });
        userButton = (Button) findViewById(R.id.nameButton);

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = uName.getText().toString();
                Intent userUpdate = new Intent(AccountSettings.this, UsernameUpdate.class);
                userUpdate.putExtra("name", username);
                startActivity(userUpdate);
            }
        });
        imageButton = (Button) findViewById(R.id.imgButton);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent galleryIntent = new Intent ();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);*/
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AccountSettings.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog = new ProgressDialog(AccountSettings.this);
                progressDialog.setTitle("Uploading Profile Image");
                progressDialog.setMessage("Please wait");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                Uri resultUri = result.getUri();
                String currentUserId = currentUser.getUid();
                final StorageReference filepath = profileImageStorage.child("profile_images").child(currentUserId+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                                userDatabase.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AccountSettings.this, "Success", Toast.LENGTH_SHORT).show();
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(AccountSettings.this, "Failure", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
