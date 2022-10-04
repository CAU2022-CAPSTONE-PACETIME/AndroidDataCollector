package com.capstone.breathdatacollector;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    public static MutableLiveData<Boolean> isCaliEnd = new MutableLiveData<Boolean>(true);
    public static MutableLiveData<Boolean> isDCEnd = new MutableLiveData<Boolean>(true);
    //    public static MutableLiveData<Boolean> isCaliClicked = new MutableLiveData<Boolean>(false);
//    public static MutableLiveData<Boolean> isDCClicked = new MutableLiveData<Boolean>(false);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isBluetoothOn = BluetoothHelper.checkBluetoothEnabled(MainActivity.this); //블루투스가 켜져있는지 항상 확인할 수 있으면 좋을듯 livedata라든지 해서


        SensorHelper sensorManager = new SensorHelper(MainActivity.this);

        Button btnDataCollect = findViewById(R.id.button1);
        Button btnCalibrate = findViewById(R.id.button2);

        if(this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityResultLauncher<String> permissionLauncher = this.registerForActivityResult(new ActivityResultContracts.RequestPermission(), (isGranted) -> {
                if (!isGranted) {
                    new AlertDialog.Builder(this.getApplicationContext())
                            .setTitle("저장소 접근 권한")
                            .setMessage("앱을 사용하시려면, 저장소 권한을 허용해 주세요.")
                            .setPositiveButton("확인", (DialogInterface dialog, int which)->{
                                        Intent intentAuth = new Intent();
                                        intentAuth.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package",
                                                BuildConfig.APPLICATION_ID, null);
                                        intentAuth.setData(uri);
                                        intentAuth.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        this.startActivity(intentAuth);
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

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE).setType("text/plain");
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH:mm:ss")) + ".txt";
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                TextView time = findViewById(R.id.time);
                time.setText("seconds remaining: " +(millisUntilFinished / 1000));
            }
            public void onFinish() {
                //stop collecting
                BufferedOutputStream bs = null;
                try{
                    bs = new BufferedOutputStream(new FileOutputStream(fileName));
                    String str = new String();//수현이 데이터에서 파일에 적을 문자열 받아오기
                    bs.write(str.getBytes());
                    bs.close();
                } catch (IOException e) {
                    e.getStackTrace();
                }
                TextView time = findViewById(R.id.time);
                time.setText("Collecting time: 60s");
            }
        };


        //데이터를 파일에 저장
        ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    //result.getResultCode()를 통하여 결과값 확인
                    if(result.getResultCode() == RESULT_OK) {
                        isDCEnd.setValue(false);
                        //데이터 모으는 메소드 call 추가
                        countDownTimer.start();
//                    BufferedOutputStream bs = null;
//                    try{
//                        bs = new BufferedOutputStream(new FileOutputStream(fileName));
//                        String str = new String();//수현이 데이터에서 파일에 적을 문자열 받아오기
//                        bs.write(str.getBytes());
//                        bs.close();
//                    } catch (IOException e) {
//                        e.getStackTrace();
//                    }
                    }
                    if(result.getResultCode() == RESULT_CANCELED){
                    }
                }
        );



        btnDataCollect.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view){
//                isDCClicked.setValue(true);
//                boolean isBluetoothOn = BluetoothHelper.checkBluetoothEnabled(MainActivity.this);
                if (isBluetoothOn){

//                if(true){ //임시로, 이거 지울거임
                    if(isDCEnd.getValue()){
                        mStartForResult.launch(intent); //여기를 어떻게 할지 고민이 되네. 데이터를 다 받아온 다음에
//                        해야 데이터를 파일에 write해야 하는데... launch하는 순간 intent(파일 create하는 내용 담음) 조건이
//                        성립하고, 바로 write를 시작하는데 그 때는 데이터 수집이 막 시작한 때라 모은 데이터가 없다....
//                        isDCEnd.setValue(false);
//                        //데이터 모으는 메소드 call 추가
//                        countDownTimer.start();
                    }
                    else{
                        isDCEnd.setValue(true);
                        //데이터 수집 중지 메소드 call 추가
                        countDownTimer.cancel();
                    }
                }
                else{
                    return;
                }
            }
        });
        btnCalibrate.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //click 하면 click 할 때 변하는 변수 추가
//                isCaliClicked.setValue(true);
//                boolean isBluetoothOn = BluetoothHelper.checkBluetoothEnabled(MainActivity.this);
                if (isBluetoothOn){
//                if(true){ //임시로, 이거 지울거임
                    if(isCaliEnd.getValue()){
                        isCaliEnd.setValue(false);
                        //데이터 모으는 메소드 call 추가
                    }
                    else{
                        isCaliEnd.setValue(true);
                        //데이터 수집 중지 메소드 call 추가
                    }
                }
                else{
                    return;
                }
            }}));

        isCaliEnd.observe(this, new Observer<Boolean>() {
            public void onChanged(Boolean caliState){
                btnDataCollect.setEnabled(caliState);
                if(caliState){
                    btnCalibrate.setText("START CALIBRATION");
                }
                else{
                    btnCalibrate.setText("STOP CALIBRATION");
                }
            }
        });
        isDCEnd.observe(this, new Observer<Boolean>() {
            public void onChanged(Boolean DCState){
                btnCalibrate.setEnabled(DCState);
                if(DCState){
                    btnDataCollect.setText("START COLLECTING DATA");
                    TextView time = findViewById(R.id.time);
                    time.setText("Collecting time: 60s");
                }
                else{
                    btnDataCollect.setText("STOP COLLECTING");
                }
            }
        });
//        isCaliClicked.observe(this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean caliClickedState) {
//                if(caliClickedState){
//                    boolean isBluetoothOn = BluetoothHelper.checkBluetoothEnabled(MainActivity.this);
//                    if (isBluetoothOn){
//                        if(isCaliEnd.getValue()){
//                            isCaliEnd.setValue(false);
//                            //데이터 모으는 메소드 call 추가
//                        }
//                        else{
//                            isCaliEnd.setValue(true);
//                            //데이터 수집 중지 메소드 call 추가
//                        }
//                    }
//                    else{
//                        return;
//                    }
//                    isCaliClicked.setValue(false);
//                }
//            }
//        });
//        isDCClicked.observe(this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean DCClickedState) {
//                if(DCClickedState){
//                    boolean isBluetoothOn = BluetoothHelper.checkBluetoothEnabled(MainActivity.this);
//                    if (isBluetoothOn){
//                        if(isDCEnd.getValue()){
//                            isDCEnd.setValue(false);
//                            //데이터 모으는 메소드 call 추가
//                        }
//                        else{
//                            isDCEnd.setValue(true);
//                            //데이터 수집 중지 메소드 call 추가
//                        }
//                    }
//                    else{
//                        return;
//                    }
//                    isDCClicked.setValue(false);
//                }
//            }
//        });
    }
}