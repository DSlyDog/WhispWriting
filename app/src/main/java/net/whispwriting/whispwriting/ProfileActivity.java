package net.whispwriting.whispwriting;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

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
    private ProgressDialog progress;
    private int currentState;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar usersToolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(usersToolbar);
        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String uid = getIntent().getStringExtra("userID");

        firestore = FirebaseFirestore.getInstance();
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

        DocumentReference user = firestore.collection("Users").document(uid);
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.getString("name");
                        String userStatus = dataSnapshot.getString("status");
                        String image = dataSnapshot.getString("image");

                        displayName.setText(name);
                        status.setText(userStatus);
                        Picasso.get().load(image).placeholder(R.drawable.avatar).into(userImage);
                    }
                }
                DocumentReference friendRequests = firestore.collection("Friend_Requests").document(currentUser.getUid());
                friendRequests.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot dataSnapshot = task.getResult();
                            if (dataSnapshot.exists()) {
                                String requestType = dataSnapshot.getString(uid + "request_type");
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
                        }
                    }
                });

                DocumentReference friends = firestore.collection("Users").document(currentUser.getUid());
                friendRequests.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot dataSnapshot = task.getResult();
                            if (dataSnapshot.exists()) {
                                List<String> friends = (ArrayList<String>) dataSnapshot.get("friends");
                                if (friends.contains(uid)) {
                                    currentState = 3;
                                    sendFriendRequest.setText("Unfriend");
                                }
                            }
                        }
                    }
                });
            }
        });
        if (currentUser.getUid().equals(uid)){
            sendFriendRequest.setEnabled(false);
            sendFriendRequest.setVisibility(View.INVISIBLE);
        }
        /*sendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                sendFriendRequest.setEnabled(false);
                if (currentState == 0){
                    DocumentReference notificationRef = firestore.collection("notifications").document(uid);
                    String notificationID = notificationRef.getId();

                    /*Map<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");
                    Map<String, Map<String, String>> notifications = new HashMap<>();
                    notifications.put(uid + "/" + notificationID, notificationData);
                    notificationRef.set(notifications);//

                    DocumentReference userRef = firestore.collection("Friend_Requests").document(currentUser.getUid());
                    Map<String, Object> requestMap = new HashMap();
                    requestMap.put(currentUser.getUid() + "/" + uid + "request_type", "sent");
                    requestMap.put(uid + "/" + currentUser.getUid() + "request_type", "received");

                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()){
                                    sendFriendRequest.setEnabled(true);
                                    currentState = 1;
                                    sendFriendRequest.setText("Cancel Request");
                                    Toast.makeText(ProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(ProfileActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }else if (currentState == 1){
                    DocumentReference userRef = firestore.collection("Friend_Requests").document(currentUser.getUid());
                    Map<String, Object> requestMap = new HashMap();
                    requestMap.put(currentUser.getUid() + "/" + uid + "request_type", null);
                    requestMap.put(uid + "/" + currentUser.getUid() + "request_type", null);

                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()){
                                    sendFriendRequest.setEnabled(true);
                                    currentState = 0;
                                    sendFriendRequest.setText("Send Friend Request");
                                    Toast.makeText(ProfileActivity.this, "Friend request canceled", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(ProfileActivity.this, "Failed to cancel friend request", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }else if (currentState == 2){
                    DocumentReference userRef = firestore.collection("Friend_Requests").document(currentUser.getUid());
                    Map<String, Object> requestMap = new HashMap();
                    requestMap.put(currentUser.getUid() + "/" + uid + "request_type", null);
                    requestMap.put(uid + "/" + currentUser.getUid() + "request_type", null);

                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()){
                                    sendFriendRequest.setEnabled(true);
                                    currentState = 0;
                                    sendFriendRequest.setText("Send Friend Request");
                                    DocumentReference userDoc = firestore.collection("Users").document(uid);
                                    DocumentReference selfUserDoc = firestore.collection("Users").document(currentUser.getUid());

                                    List<String> userFriends = new ArrayList<>();
                                    List<String> selfFriends = new ArrayList<>();
                                    userFriends.add(currentUser.getUid());
                                    selfFriends.add(uid);

                                    userDoc.set(userFriends, SetOptions.mergeFields("friends"));
                                    selfUserDoc.set(selfFriends, SetOptions.mergeFields("friends"));

                                    Toast.makeText(ProfileActivity.this, "Friend request canceled", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(ProfileActivity.this, "Failed to cancel friend request", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }else if (currentState == 3){
                    DocumentReference userRef = firestore.collection("Users").document(currentUser.getUid());
                    Map<String, Object> requestMap = new HashMap();
                    requestMap.put(currentUser.getUid() + "/" + uid, null);
                    requestMap.put(uid + "/" + currentUser.getUid(), null);

                    rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null){
                                sendFriendRequest.setEnabled(true);
                                currentState = 0;
                                sendFriendRequest.setText("Send Friend Request");
                                Toast.makeText(ProfileActivity.this, "You are no longer friends with this user.", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed to unfriend this user", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });*/

        /*declineFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> requestMap = new HashMap();
                requestMap.put("Friend_Requests/" + currentUser.getUid() + "/" + uid + "request_type", null);
                requestMap.put("Friend_Requests/" + uid + "/" + currentUser.getUid() + "request_type", null);

                rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null){
                            sendFriendRequest.setEnabled(true);
                            currentState = 0;
                            sendFriendRequest.setText("Send Friend Request");
                            declineFriendRequest.setEnabled(false);
                            declineFriendRequest.setVisibility(View.INVISIBLE);
                            Toast.makeText(ProfileActivity.this, "Friend request declined", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ProfileActivity.this, "Failed to decline friend request", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });*/

    }
}
