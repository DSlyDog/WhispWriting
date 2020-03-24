package net.whispwriting.whispwriting;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

public class chat extends maindrawer {

        private ViewPager rViewPager;
        private PagerSection rPagerSections;
        private TabLayout cTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar1 = (Toolbar) findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar1);
        getSupportActionBar().setTitle("Chat");
        Intent intent = new Intent(this, chat.class);
        lastIntent = intent;

        //Tabs
        rViewPager = (ViewPager) findViewById(R.id.mainTabPager);
        rPagerSections = new PagerSection(getSupportFragmentManager());

        rViewPager.setAdapter(rPagerSections);

        cTabLayout = (TabLayout) findViewById(R.id.chatTabs);
        cTabLayout.setupWithViewPager(rViewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar1, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            Intent loginSplash = new Intent(this, login.class);
            startActivity(loginSplash);
        }
        if (item.getItemId() == R.id.action_accounts){
            Intent accountSettings = new Intent(this, AccountSettings.class);
            startActivity(accountSettings);
        }
        if (item.getItemId() == R.id.action_usersList){
            Intent userList = new Intent(this, UserList.class);
            startActivity(userList);
        }

        return true;
    }
}
