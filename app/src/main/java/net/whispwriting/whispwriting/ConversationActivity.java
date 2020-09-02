package net.whispwriting.whispwriting;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

public class ConversationActivity extends AppCompatActivity {

    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Toolbar convToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(convToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = getIntent().getStringExtra("userID");
    }
}