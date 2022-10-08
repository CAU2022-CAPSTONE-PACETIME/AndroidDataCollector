package com.capstone.breathdatacollector;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    public static MutableLiveData<Boolean> isCaliEnd = new MutableLiveData<Boolean>(true);
    public static MutableLiveData<Boolean> isDCEnd = new MutableLiveData<Boolean>(true);

    public boolean isOnCreateEnd = false;

    public Intent intentData;
    public Intent intentCali;
//    private ActivityResultLauncher<Intent> mStartForResultData = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if(result.getResultCode() == RESULT_OK) {
//
//                    String str = sensorHelper.getBreathData();
//                    if(str == null){
//                        Toast noDataAlarm = Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG);
//                    }
//                    else{
//                        try {
//                            ParcelFileDescriptor pfd = MainActivity.this.getContentResolver().
//                                    openFileDescriptor(result.getData().getData(), "w");
//                            FileOutputStream fileOutputStream =
//                                    new FileOutputStream(pfd.getFileDescriptor());
//                            fileOutputStream.write(str.getBytes());
//                            fileOutputStream.close();
//                            pfd.close();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                if(result.getResultCode() == RESULT_CANCELED){
//                }
//            }
//    );

//    private ActivityResultLauncher<Intent> mStartForResultCali = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if(result.getResultCode() == RESULT_OK) {
//
//                    String str = sensorHelper.getCaliData().toString();
//                    if(str == null){
//                        Toast noDataAlarm = Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG);
//                    }
//                    else{
//                        try {
//                            ParcelFileDescriptor pfd = MainActivity.this.getContentResolver().
//                                    openFileDescriptor(result.getData().getData(), "w");
//                            FileOutputStream fileOutputStream =
//                                    new FileOutputStream(pfd.getFileDescriptor());
//                            fileOutputStream.write(str.getBytes());
//                            fileOutputStream.close();
//                            pfd.close();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                if(result.getResultCode() == RESULT_CANCELED){
//                }
//            }
//    );

    private ActivityResultLauncher<Intent> mStartForResultData;
    private ActivityResultLauncher<Intent> mStartForResultCali;

//    private void mStartForResultData(Intent intentData, String strData){
//        if(strData == null){
//            Toast noDataAlarm = Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG);
//            return;
//        }
//        else{
//            registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if(result.getResultCode() == RESULT_OK) {
//                        try {
//                            ParcelFileDescriptor pfd = MainActivity.this.getContentResolver().
//                                    openFileDescriptor(result.getData().getData(), "w");
//                            FileOutputStream fileOutputStream =
//                                    new FileOutputStream(pfd.getFileDescriptor());
//                            fileOutputStream.write(strData.getBytes());
//                            fileOutputStream.close();
//                            pfd.close();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    if(result.getResultCode() == RESULT_CANCELED){
//                    }
//                }
//            ).launch(intentData);
//        }
//    }
//
//    private void mStartForResultCali(Intent intentCali, String strCali){
//        if(strCali == null){
//            Toast noDataAlarm = Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG);
//            return;
//        }
//        else{
//            registerForActivityResult(
//                    new ActivityResultContracts.StartActivityForResult(),
//                    result -> {
//                        if(result.getResultCode() == RESULT_OK) {
//                            try {
//                                ParcelFileDescriptor pfd = MainActivity.this.getContentResolver().
//                                        openFileDescriptor(result.getData().getData(), "w");
//                                FileOutputStream fileOutputStream =
//                                        new FileOutputStream(pfd.getFileDescriptor());
//                                fileOutputStream.write(strCali.getBytes());
//                                fileOutputStream.close();
//                                pfd.close();
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        if(result.getResultCode() == RESULT_CANCELED){
//                        }
//                    }
//            ).launch(intentCali);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorHelper sensorHelper = new SensorHelper(MainActivity.this);

        Button btnDataCollect = findViewById(R.id.button1);
        Button btnCalibrate = findViewById(R.id.button2);

//        if(this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            ActivityResultLauncher<String> permissionLauncher = this.registerForActivityResult(new ActivityResultContracts.RequestPermission(), (isGranted) -> {
//                if (!isGranted) {
//                    new AlertDialog.Builder(this.getApplicationContext())
//                            .setTitle("저장소 접근 권한")
//                            .setMessage("앱을 사용하시려면, 저장소 권한을 허용해 주세요.")
//                            .setPositiveButton("확인", (DialogInterface dialog, int which)->{
//                                        Intent intentAuth = new Intent();
//                                        intentAuth.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        Uri uri = Uri.fromParts("package",
//                                                BuildConfig.APPLICATION_ID, null);
//                                        intentAuth.setData(uri);
//                                        intentAuth.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        this.startActivity(intentAuth);
//                                    }
//                            )
//                            .create()
//                            .show();
//                }
//            });
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            }
//        }

        mStartForResultData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    try {
                        ParcelFileDescriptor pfd = MainActivity.this.getContentResolver().
                                openFileDescriptor(result.getData().getData(), "w");
                        FileOutputStream fileOutputStream =
                                new FileOutputStream(pfd.getFileDescriptor());
                        fileOutputStream.write(sensorHelper.getBreathData().getBytes());
                        fileOutputStream.close();
                        pfd.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(result.getResultCode() == RESULT_CANCELED){
                }
            }
        );

        mStartForResultCali = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    try {
                        ParcelFileDescriptor pfd = MainActivity.this.getContentResolver().
                                openFileDescriptor(result.getData().getData(), "w");
                        FileOutputStream fileOutputStream =
                                new FileOutputStream(pfd.getFileDescriptor());
                        fileOutputStream.write(sensorHelper.getCaliData().toString().getBytes());
                        fileOutputStream.close();
                        pfd.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(result.getResultCode() == RESULT_CANCELED){
                }
            }
        );

        intentData = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intentData.addCategory(Intent.CATEGORY_OPENABLE).setType("text/csv");


        intentCali = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intentCali.addCategory(Intent.CATEGORY_OPENABLE).setType("text/csv");


        CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                TextView time = findViewById(R.id.time);
                time.setText("seconds remaining: " +(millisUntilFinished / 1000));
            }
            public void onFinish() {
                isDCEnd.setValue(true);
                btnDataCollect.setSelected(false);
                btnDataCollect.setText("START COLLECTING DATA");
                TextView time = findViewById(R.id.time);
                time.setText("Collecting time: 60s");
                if(sensorHelper.getBreathData() == null){
                    Toast noDataAlarm = Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG);
                    noDataAlarm.show();
                }
                else{
                    String fileNameData = "Data_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")) + ".csv";
                    intentData.putExtra(Intent.EXTRA_TITLE, fileNameData);
                    mStartForResultData.launch(intentData);
                }
            }
        };


        btnDataCollect.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view) {

                if (isDCEnd.getValue()) {
                    isDCEnd.setValue(false);
                    sensorHelper.doCollectData();
                    countDownTimer.start();
                } else {
                    isDCEnd.setValue(true);
                    countDownTimer.cancel();
                    if(sensorHelper.getBreathData() == null){
                        Toast noDataAlarm = Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG);
                        noDataAlarm.show();
                    }
                    else{
                        String fileNameData = "Data_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")) + ".csv";
                        intentData.putExtra(Intent.EXTRA_TITLE, fileNameData);
                        mStartForResultData.launch(intentData);
                    }
                }

            }});
        btnCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCaliEnd.getValue()) {
                    sensorHelper.calibrate();
                    isCaliEnd.setValue(false);
                } else {
                    isCaliEnd.setValue(true);
//                    mStartForResultCali.launch(intentCali); //observe에 넣을거면 여긴 안해도 될거 같긴하네.
                }
            }});

        isCaliEnd.observe(this, new Observer<Boolean>() {
            public void onChanged(Boolean caliState){
                btnDataCollect.setEnabled(caliState);
                if(caliState){
                    btnCalibrate.setSelected(false);
                    btnCalibrate.setText("START CALIBRATION");
                    if(sensorHelper.getCaliData().toString() == null){
                        if(isOnCreateEnd){

                            Toast noDataAlarm = Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG);
                            noDataAlarm.show();

                            Log.d("CALIDATANULL", "CaliData = ");
                        }
                        isOnCreateEnd = true;
                    }
                    else{
                        Log.d("CALIDATANOTNULL", "CaliData = " + sensorHelper.getCaliData());
                        String fileNameCali = "Cali_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")) + ".csv";
                        intentCali.putExtra(Intent.EXTRA_TITLE, fileNameCali);
                        mStartForResultCali.launch(intentCali);
                    }
                }
                else{
                    btnCalibrate.setSelected(true);
                    btnCalibrate.setText("STOP CALIBRATION");
                }
            }
        });
        isDCEnd.observe(this, new Observer<Boolean>() {
            public void onChanged(Boolean DCState){
                btnCalibrate.setEnabled(DCState);
                if(DCState){
                    btnDataCollect.setSelected(false);
                    btnDataCollect.setText("START COLLECTING DATA");
                    TextView time = findViewById(R.id.time);
                    time.setText("Collecting time: 60s");
                }
                else{
                    btnDataCollect.setSelected(true);
                    btnDataCollect.setText("STOP COLLECTING");
                }
            }
        });
    }
}