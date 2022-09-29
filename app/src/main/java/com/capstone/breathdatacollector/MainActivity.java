package com.capstone.breathdatacollector;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDataCollect = findViewById(R.id.button1);
        Button btnCalibrate = findViewById(R.id.button2);
        btnDataCollect.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view){
                boolean isBluetoothOn = BluetoothHelper.checkBluetoothEnabled(this);
                if (isBluetoothOn){
                    SensorManager sensorManager = SensorManager.getInstance();
                    sensorManager.setContext(this);
                    //데이터 몹는 메소드 call 추가
                }
                else{
                    return;
                }
            }
        });
        btnCalibrate.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isBluetoothOn = BluetoothHelper.checkBluetoothEnabled(this);
                if (isBluetoothOn){
                    SensorCalibrator sensorCalibrator = SensorCalibrator.getInstance();
                    sensorCalibrator.setContext(this);
                    //데이터 몹는 메소드 call 추가
                }
                else{
                    return;
                }
            }
        }));

    }
}