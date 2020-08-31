package net.whispwriting.whispwriting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView usersListPage;
    private DatabaseReference usersDatabase, rootRef;
    private CircleImageView userListImg;
    private String name, status, image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Toolbar usersToolbar = findViewById(R.id.FriendListToolbar);
        setSupportActionBar(usersToolbar);
        getSupportActionBar().setTitle("Friends");

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Friends")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        rootRef = FirebaseDatabase.getInstance().getReference();

        usersListPage = (RecyclerView) findViewById(R.id.FriendListPage);
        usersListPage.setHasFixedSize(true);
        usersListPage.setLayoutManager(new LinearLayoutManager(this));

        userListImg = (CircleImageView) findViewById(R.id.userListImg);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, usersToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(usersDatabase, new SnapshotParser<Users>() {
            @NonNull
            @Override
            public Users parseSnapshot(@NonNull DataSnapshot snapshot) {
                System.out.println(snapshot);
                rootRef.child("Users").child(snapshot.getValue().toString()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        System.out.println(dataSnapshot);
                        name = dataSnapshot.child("name").getValue().toString();
                        status = dataSnapshot.child("status").getValue().toString();
                        image = dataSnapshot.child("image").getValue().toString();
                        return;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                System.out.println(snapshot);
                return new Users(name, image, status);
            }
        }).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int i, @NonNull Users users) {
                usersViewHolder.setName(users.name);
                usersViewHolder.setStatus(users.status);
                usersViewHolder.setImg(users.image);

                final String userID = getRef(i).getKey();
                usersViewHolder.mView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Intent profilePage = new Intent(FriendsList.this, ProfileActivity.class);
                        profilePage.putExtra("userID", userID);
                        startActivity(profilePage);
                    }
                });
            }

        };
        usersListPage.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_chat) {
            // Handle the camera action
            if (FirebaseAuth.getInstance().getCurrentUser() == null){
                startActivity(new Intent(this, login.class));
            }
            Intent intent = new Intent (this, Chat.class);
            startActivity(intent);
        }
        if (id == R.id.nav_friends){
            startActivity(new Intent(this, FriendsList.class));
        }
        if (id == R.id.nav_search_users){
            startActivity(new Intent(this, UserList.class));
        }
        if (id == R.id.nav_share) {

        }

        return true;
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setName (String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.userListName);
            userNameView.setText(name);
        }
        public void setStatus (String status) {
            TextView userStatusVIew = (TextView) mView.findViewById(R.id.userListStatus);
            userStatusVIew.setText(status);
        }
        public void setImg (String image){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.userListImg);
            Picasso.get().load(image).placeholder(R.drawable.avatar).into(userImageView);
        }
    }
}
