package com.capstone.breathdatacollector;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class MainActivity extends AppCompatActivity {

    public static MutableLiveData<Boolean> isCaliEnd = new MutableLiveData<Boolean>(true);
    public static MutableLiveData<Boolean> isDCEnd = new MutableLiveData<Boolean>(true);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
             ActivityResultLauncher<String> permissionLauncher = this.registerForActivityResult(new ActivityResultContracts.RequestPermission(), (isGranted) -> {
                if (!isGranted) {
                    new AlertDialog.Builder(this.getApplicationContext())
                            .setTitle("저장소 접근 권한")
                            .setMessage("앱을 사용하시려면, 저장소 권한을 허용해 주세요.")
                            .setPositiveButton("확인", (DialogInterface dialog, int which)->{
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package",
                                                BuildConfig.APPLICATION_ID, null);
                                        intent.setData(uri);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        this.startActivity(intent);
                                    }
                            )
                            .create()
                            .show();
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        //데이터를 파일에 저장


        Button btnDataCollect = findViewById(R.id.button1);
        Button btnCalibrate = findViewById(R.id.button2);
        btnDataCollect.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view){
                boolean isBluetoothOn = BluetoothHelper.checkBluetoothEnabled(this);
                if (isBluetoothOn){
                    isDCEnd.setValue(false);
                    SensorHelper sensorManager = new SensorHelper(this);
                    //데이터 몹는 메소드 call 추가

                    new CountDownTimer(60000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            TextView time = findViewById(R.id.time);
                            time.setText("seconds remaining: " +(millisUntilFinished / 1000));
                        }
                        public void onFinish() {
                            //stop collecting
                            TextView time = findViewById(R.id.time);
                            time.setText("Collecting time: 60s");
                        }
                    }.start();
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
                    SensorHelper sensorManager = new SensorHelper(this);
                    //데이터 몹는 메소드 call 추가
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