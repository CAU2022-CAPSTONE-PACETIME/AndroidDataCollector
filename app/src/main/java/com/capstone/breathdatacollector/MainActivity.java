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
    public static MutableLiveData<Boolean> isConvertEnd = new MutableLiveData<Boolean>(true);

    public boolean isOnCreateEnd;

    public Intent intentData;
    public Intent intentCali;

    private ActivityResultLauncher<Intent> mStartForResultData;
    private ActivityResultLauncher<Intent> mStartForResultCali;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorHelper sensorHelper = new SensorHelper(MainActivity.this);

        Button btnDataCollect = findViewById(R.id.button1);
        Button btnCalibrate = findViewById(R.id.button2);

        mStartForResultData = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
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
                    if (result.getResultCode() == RESULT_CANCELED) {
                    }
                }
        );

        mStartForResultCali = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
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
                    if (result.getResultCode() == RESULT_CANCELED) {
                    }
                }
        );

        intentData = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intentData.addCategory(Intent.CATEGORY_OPENABLE).setType("text/csv");

        intentCali = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intentCali.addCategory(Intent.CATEGORY_OPENABLE).setType("text/csv");

        CountDownTimer countDownTimer = new CountDownTimer(13000, 1000) {

            public void onTick(long millisUntilFinished) {
                TextView time = findViewById(R.id.time);
                if(millisUntilFinished >= 10200){
                    time.setText("collect after " +((millisUntilFinished - 10000) / 1000) + "s");
                }
                else {
                    if(millisUntilFinished >= 9800){
                        sensorHelper.doCollectData();
                    }
                    time.setText("seconds remaining: " + ((millisUntilFinished) / 1000));
                }
            }

            public void onFinish() {
                isDCEnd.setValue(true);
//                btnDataCollect.setSelected(false);
//                btnDataCollect.setText("START COLLECTING DATA");
//                TextView time = findViewById(R.id.time);
//                time.setText("Collecting time: 60s");
//                if (sensorHelper.getBreathData() == null) {
//                    Log.d("NULLDATA", "Data collecting failed");
//                    MainActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } else {
//                    String fileNameData = "Data_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")) + ".csv";
//                    intentData.putExtra(Intent.EXTRA_TITLE, fileNameData);
//                    mStartForResultData.launch(intentData);
//                } //1, 2 주석 지우고 3 (if,else) 주석 넣어봐,   여기가 1
            }
        };


        btnDataCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isDCEnd.getValue()) {
                    isDCEnd.setValue(false);
//                    sensorHelper.doCollectData(); //onTick에 docollectdata 넣을거면 이건 주석처리해야함.
                    countDownTimer.start();
                }
                else {
                    isDCEnd.setValue(true);
                    countDownTimer.cancel();

//                    if (sensorHelper.getBreathData() == null) {
//                        MainActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG).show();
//                            }
//                        });
//                    }
//                    else {
//                        String fileNameData = "Data_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")) + ".csv";
//                        intentData.putExtra(Intent.EXTRA_TITLE, fileNameData);
//                        mStartForResultData.launch(intentData);
//                    } // 여기가 2
                }
            }
        });

        btnCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCaliEnd.getValue()) {
                    sensorHelper.calibrate();
                    isCaliEnd.setValue(false);
                }
                else {
                    isCaliEnd.setValue(true);
                }
            }
        });

        isDCEnd.observe(this, new Observer<Boolean>() {
            public void onChanged(Boolean DCState) {
                btnCalibrate.setEnabled(DCState);
                if (DCState) {
                    btnDataCollect.setSelected(false);
                    btnDataCollect.setText("START COLLECTING DATA");
                    TextView time = findViewById(R.id.time);
                    time.setText("Collecting time: 60s");

//                    Log.d("DCENDCHECK", "isDCEnd = " + String.valueOf(DCState));
//                    Log.d("SDCSTARTCHECK", "sH.isDCStart = " + String.valueOf(sensorHelper.isDCStart));
//                    Log.d("SDCENDCHECK", "sH.isDCEnd = " + String.valueOf(sensorHelper.isDCEnd));


//                    if(sensorHelper.getBreathData() == null){
////                        Log.d("CHECKBOOL", "isOnCreated = " + String.valueOf(isOnCreateEnd));
//                        Log.d("NULLDATA", "Data collecting failed");
//                        if(isOnCreateEnd){
//                            MainActivity.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }
//                    }
//                    else{
//                        String fileNameData = "Data_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")) + ".csv";
//                        intentData.putExtra(Intent.EXTRA_TITLE, fileNameData);
//                        mStartForResultData.launch(intentData);
//                    } //여기 if~else 총 9줄이 3
                }
                else {
                    btnDataCollect.setSelected(true);
                    btnDataCollect.setText("STOP COLLECTING");
                }
            }
        });

        isCaliEnd.observe(this, new Observer<Boolean>() {
            public void onChanged(Boolean caliState) {
                btnDataCollect.setEnabled(caliState);
                if (caliState) {
                    btnCalibrate.setSelected(false);
                    btnCalibrate.setText("START CALIBRATION");
                    if (sensorHelper.getCaliData().toString() == null) {
                        Log.d("CHECKBOOL", "isOnCreated = " + String.valueOf(isOnCreateEnd));
                        if (isOnCreateEnd) {

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                    else {
                        String fileNameCali = "Cali_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")) + ".csv";
                        intentCali.putExtra(Intent.EXTRA_TITLE, fileNameCali);
                        mStartForResultCali.launch(intentCali);
                    }
                }
                else {
                    btnCalibrate.setSelected(true);
                    btnCalibrate.setText("STOP CALIBRATION");
                }
            }
        });

        isConvertEnd.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean convertState) {
                if(convertState){
                    if(sensorHelper.getBreathData() == null){
//                        Log.d("CHECKBOOL", "isOnCreated = " + String.valueOf(isOnCreateEnd));
                        Log.d("NULLDATA", "Data collecting failed");
                        if(isOnCreateEnd){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "데이터가 수집되지 않았습니다.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        isOnCreateEnd = true;
                    }
                    else{
                        String fileNameData = "Data_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")) + ".csv";
                        intentData.putExtra(Intent.EXTRA_TITLE, fileNameData);
                        mStartForResultData.launch(intentData);
                    }
                }
                else{
                }
            }
        });
    }
}