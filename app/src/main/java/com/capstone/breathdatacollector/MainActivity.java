package com.capstone.breathdatacollector;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.lifecycle.Observer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

public class MainActivity extends AppCompatActivity {

    public static MutableLiveData<Boolean> isCaliEnd = new MutableLiveData<Boolean>(true);
    public static MutableLiveData<Boolean> isDCEnd = new MutableLiveData<Boolean>(true);
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
                    isDCEnd.setValue(false);
                    SensorManager sensorManager = new SensorManager(this);
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
                    isCaliEnd.setValue(false);
                    SensorCalibrator sensorCalibrator = new SensorCalibrator(this);
                    //데이터 몹는 메소드 call 추가
                    //데이터 저장


                }
                else{
                    return;
                }
            }
        }));

        isCaliEnd.observe(this, new Observer<Boolean>() {
            public void onChanged(Boolean caliState){
                btnCalibrate.setEnabled(caliState);
            }
        });
        isDCEnd.observe(this, new Observer<Boolean>() {
            public void onChanged(Boolean DCState){
                btnDataCollect.setEnabled(DCState);
            }
        });

    }
}