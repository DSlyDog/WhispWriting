package net.whispwriting.whispwriting;

import android.app.ProgressDialog;
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
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView displayName;
    private TextView status;
    private CircleImageView userImage;
    private Button sendFriendRequest;
    private Button declineFriendRequest;
    private DatabaseReference userDatabase;
    private ProgressDialog progress;
    private int currentState;
    private DatabaseReference friendRequestDatabase;
    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar usersToolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(usersToolbar);
        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String uid = getIntent().getStringExtra("userID");
        System.out.println("uid: " + uid);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        displayName = (TextView) findViewById(R.id.DisplayNameText_other);
        status = (TextView) findViewById(R.id.statusText_other);
        userImage = (CircleImageView) findViewById(R.id.profile_image_other);
        sendFriendRequest = (Button) findViewById(R.id.friendRequest);
        declineFriendRequest = (Button) findViewById(R.id.declineFriendRequest);
        declineFriendRequest.setEnabled(false);
        declineFriendRequest.setVisibility(View.INVISIBLE);

        currentState = 0;

        progress = new ProgressDialog(this);
        progress.setTitle("Loading User Data");
        progress.setMessage("Please wait while the user's data is loaded");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String userStatus = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                displayName.setText(name);
                status.setText(userStatus);
                Picasso.get().load(image).placeholder(R.drawable.avatar).into(userImage);

                friendRequestDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(uid)){
                            String requestType = dataSnapshot.child(uid).child("request_type").getValue().toString();
                            if (requestType.equals("received")){
                                currentState = 2;
                                sendFriendRequest.setText("Accept Friend Request");
                                declineFriendRequest.setEnabled(true);
                                declineFriendRequest.setVisibility(View.VISIBLE);
                            }else if (requestType.equals("sent")){
                                currentState = 1;
                                sendFriendRequest.setText("Cancel Friend Request");
                            }
                        }

                        progress.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                friendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(uid)){
                            currentState = 3;
                            sendFriendRequest.setText("Unfriend");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (currentUser.getUid().equals(uid)){
            sendFriendRequest.setEnabled(false);
            sendFriendRequest.setVisibility(View.INVISIBLE);
        }
        sendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                sendFriendRequest.setEnabled(false);
                if (currentState == 0){
                    friendRequestDatabase.child(currentUser.getUid()).child(uid).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                friendRequestDatabase.child(uid).child(currentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", currentUser.getUid());
                                        notificationData.put("type", "request");

                                        notificationDatabase.child(uid).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    sendFriendRequest.setEnabled(true);
                                                    currentState = 1;
                                                    sendFriendRequest.setText("Cancel Request");
                                                    Toast.makeText(ProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else if (currentState == 1){
                    friendRequestDatabase.child(currentUser.getUid()).child(uid).child("request_type")
                            .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                friendRequestDatabase.child(uid).child(currentUser.getUid()).child("request_type")
                                        .setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendFriendRequest.setEnabled(true);
                                        currentState = 0;
                                        sendFriendRequest.setText("Send Friend Request");
                                        Toast.makeText(ProfileActivity.this, "Friend request canceled", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed canceling request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else if (currentState == 2){
                    declineFriendRequest.setEnabled(false);
                    declineFriendRequest.setVisibility(View.INVISIBLE);
                    final Date currentDate = new Date();
                    friendDatabase.child(currentUser.getUid()).child(uid).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendDatabase.child(uid).child(currentUser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friendRequestDatabase.child(currentUser.getUid()).child(uid).child("request_type")
                                            .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                friendRequestDatabase.child(uid).child(currentUser.getUid()).child("request_type")
                                                        .setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        sendFriendRequest.setEnabled(true);
                                                        currentState = 3;
                                                        sendFriendRequest.setText("Unfriend");
                                                        Toast.makeText(ProfileActivity.this, "Friend request accepted", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(ProfileActivity.this, "Failed accepting request", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }else if (currentState == 3){
                    friendDatabase.child(currentUser.getUid()).child(uid).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                friendDatabase.child(uid).child(currentUser.getUid()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendFriendRequest.setEnabled(true);
                                        currentState = 0;
                                        sendFriendRequest.setText("Send Friend Request");
                                        Toast.makeText(ProfileActivity.this, "You are no longer friends with this user.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        declineFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRequestDatabase.child(currentUser.getUid()).child(uid).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestDatabase.child(uid).child(currentUser.getUid()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendFriendRequest.setEnabled(true);
                                    currentState = 0;
                                    sendFriendRequest.setText("Send Friend Request");
                                    declineFriendRequest.setEnabled(false);
                                    declineFriendRequest.setVisibility(View.INVISIBLE);
                                    Toast.makeText(ProfileActivity.this, "Request denied.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

    }
}
