package com.capstone.breathdatacollector;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButton1Clicked(View v) {
        Toast.makeText(this, "저는 확인1이에요.", Toast.LENGTH_LONG).show(); // Toast는 간단한 메세지를 잠깐 보여주는 역할을 수행한다.
    }
}