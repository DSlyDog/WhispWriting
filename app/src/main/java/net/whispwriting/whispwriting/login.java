package net.whispwriting.whispwriting;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

public class login extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    private FirebaseAuth mAuth;
    private Button registryBtn;
    private Button loginBtn;
    private Toolbar rToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        rToolbar = (Toolbar) findViewById(R.id.loginToolbar);
        setSupportActionBar(rToolbar);
        getSupportActionBar().setTitle("Chat");

        mAuth = FirebaseAuth.getInstance();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, rToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            Intent intent = new Intent (this, maindrawer.class);
            startActivity(intent);
        }
        if (id == R.id.nav_literature) {
            Intent intent = new Intent(this, literature.class);
            startActivity(intent);
        }
        if (id == R.id.nav_drawings) {

        }
        if (id == R.id.nav_videos) {

        }
        if (id == R.id.nav_chat) {
            Intent intent = new Intent(this, login.class);
            startActivity(intent);
        }
        if (id == R.id.nav_share) {

        }

        return true;
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            Intent chat = new Intent(this, chat.class);
            startActivity(chat);
            finish();
        }
        else {
            registryBtn = (Button) findViewById(R.id.register);
            registryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent regIntent = new Intent(login.this, registration.class);
                    startActivity(regIntent);
                }
            });
            loginBtn = (Button) findViewById(R.id.login);
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent logIntent = new Intent(login.this, LoggingIn.class);
                    startActivity(logIntent);
                }
            });
        }
    }
}

