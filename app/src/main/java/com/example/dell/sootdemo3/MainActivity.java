package com.example.dell.sootdemo3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button alterText;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alterText = findViewById(R.id.button);
        textView = findViewById(R.id.txt);
        alterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("no hello");
            }
        });
        User user = new User();
        user.setName("feng");
        user.setAge(20);
        user.isLow();
    }
}
