package net.whispwriting.whispwriting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserList extends AppCompatActivity {

    private RecyclerView usersListPage;
    private DatabaseReference usersDatabase;
    private CircleImageView userListImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Toolbar usersToolbar = findViewById(R.id.UserListToolbar);
        setSupportActionBar(usersToolbar);
        getSupportActionBar().setTitle("Users List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        usersListPage = (RecyclerView) findViewById(R.id.UserListPage);
        usersListPage.setHasFixedSize(true);
        usersListPage.setLayoutManager(new LinearLayoutManager(this));

        userListImg = (CircleImageView) findViewById(R.id.userListImg);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(usersDatabase, Users.class).build();
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
                        Intent profilePage = new Intent(UserList.this, ProfileActivity.class);
                        profilePage.putExtra("userID", userID);
                        startActivity(profilePage);
                    }
                });
            }

        };
        usersListPage.setAdapter(adapter);
        adapter.startListening();
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
