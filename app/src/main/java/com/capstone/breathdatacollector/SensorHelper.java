package com.capstone.breathdatacollector;

import static java.lang.Math.abs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SensorHelper implements SensorEventListener {
    private static final String TAG = "SENSOR_MANAGER";

    private Sensor accSensor, gyroSensor; //, stepSensor;

    private AudioRecord audioSensor;
    private SensorManager sensorManager;
    private BluetoothHelper bluetoothHelper;

    private final Context context;
    private final MainActivity activity;

    private final int AUDIO_SAMP_RATE = 44100;
    private int recordSeconds = 60;
    private final int bufferShortSize = 4096;
    private short[] bufferRecord;
    private int bufferRecordSize;
    private final ShortBuffer shortBuffer = ShortBuffer.allocate(44100 * recordSeconds);

    private List<float[]> accData;
    private List<float[]> gyroData;
    private List<Long> imuTimeStamp;

    private CalibrationData caliData;

    private BreathData breathData;

    public SensorHelper(MainActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        bluetoothHelper = new BluetoothHelper(activity);

        caliData = null;
        breathData = null;
        checkPermissions(activity, Manifest.permission.RECORD_AUDIO, "녹음");
        checkPermissions(activity, Manifest.permission.ACTIVITY_RECOGNITION, "활동 인지");
        setCaliDataBySharedPref(activity);
    }

    private boolean checkPermissions(AppCompatActivity activity, String permission, String content){
        if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            ActivityResultLauncher<String> permissionLauncher;
            permissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), (isGranted) -> {
                if (!isGranted) {
                    new AlertDialog.Builder(activity.getApplicationContext())
                            .setTitle(content + " 권한")
                            .setMessage("앱을 사용하시려면, "+ content +" 권한을 허용해 주세요.")
                            .setPositiveButton("확인", (DialogInterface dialog, int which) -> {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package",
                                                BuildConfig.APPLICATION_ID, null);
                                        intent.setData(uri);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        activity.startActivity(intent);
                                    }
                            )
                            .create()
                            .show();
                }
            });

            permissionLauncher.launch(permission);
            return false;
        }

        return true;
    }

    private void setCaliDataBySharedPref(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("BreathData", Context.MODE_PRIVATE);

        if(sharedPref.contains("CALI")){
            caliData = CalibrationData.setData(sharedPref.getString("CALI", null));
        }
    }

    private void setSensors() {
        if (context == null) {
            Log.d(TAG, "SET_SENSOR CONTEXT is NULL");
            return;
        }

        sensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        Log.d(TAG, "Acc Min Delay: " + accSensor.getMinDelay());
        Log.d(TAG, "Gyro Min Delay: " + gyroSensor.getMinDelay());

        accData = new ArrayList<>();
        gyroData = new ArrayList<>();
        imuTimeStamp = new ArrayList<>();

//        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }


    @SuppressLint("MissingPermission")
    private void setMic() {
        if (context == null) {
            Log.d(TAG, "SET_SENSOR CONTEXT is NULL");
            return;
        }

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Log.d(TAG, "set Microphone");

        boolean isStartBtHeadset = bluetoothHelper.startBluetoothHeadset(context);
        Log.d(TAG, "is Start Bt Headset: " + isStartBtHeadset);

        if (isStartBtHeadset) {
            Log.d(TAG, "Start Bluetooth SCO");
            audioManager.startBluetoothSco();

            bufferRecordSize = AudioRecord.getMinBufferSize(AUDIO_SAMP_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            bufferRecord = new short[bufferRecordSize];

            audioSensor = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    AUDIO_SAMP_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferRecordSize);

            Log.i("AUDIO_INFO", "SampleRate: " + audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
            Log.i("AUDIO_INFO", "Buffer Size: " + audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));
            Log.i("AUDIO_INFO", "Buffer Record Size: " + bufferRecordSize);

            audioSensor.startRecording();
            shortBuffer.rewind();
            shortBuffer.position(0);
        }
        else{
            MainActivity.isCaliEnd.postValue(true);
        }
    }

    public void doCollectData(long timeInMillis){
        if(caliData == null){
            return;
        }

        if (accSensor == null || gyroSensor == null)
            setSensors();
        setMic();

        if(audioSensor == null){
            return;
        }

        Thread dataThread = new Thread(() -> {
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            long start;
            long end = 0;
            start = System.currentTimeMillis();
            while(end - start < timeInMillis && Boolean.FALSE.equals(MainActivity.isDCEnd.getValue())){
                int size = audioSensor.read(bufferRecord, 0, bufferRecordSize);

                shortBuffer.put(bufferRecord, 0, size);
                end = System.currentTimeMillis();
            }

            sensorManager.unregisterListener(this);
            stopMic();

            makeBreathData(start, end);

            MainActivity.isDCEnd.postValue(true);
        });
        dataThread.start();
    }

    public String getBreathData() {
        if(breathData == null){
            return null;
        }
        return breathData.toString();
    }

    public void calibrate() {
        if (accSensor == null || gyroSensor == null)
            setSensors();
        setMic();

        if(audioSensor == null){
            return;
        }

        Thread dataThread = new Thread(() -> {
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            boolean isRecordStart = false;
            long start = -1L;
            long end = 0;

            long soundPeakTime = 0;

            while(end - start < 1000L){
                int size = audioSensor.read(bufferRecord, 0, bufferRecordSize);
                if(!isRecordStart){
                    short maxVal = 10000;
                    int maxPos = 0;
                    for(int i = 0; i < bufferRecord.length; i++){
                        short val = bufferRecord[i];
                        if(val > maxVal){
                            maxPos = i;
                            isRecordStart = true;
                        }
                    }
                    if(isRecordStart){
                        soundPeakTime = System.currentTimeMillis() + (maxPos * 1000L) / 44100L ;
                        shortBuffer.put(bufferRecord, maxPos, size - maxPos);
                        start = System.currentTimeMillis();
                    }
                }
                else{
                    shortBuffer.put(bufferRecord, 0, size);
                    end = System.currentTimeMillis();
                }
            }
            sensorManager.unregisterListener(this);
            stopMic();

            calculateDelay(soundPeakTime);

            MainActivity.isCaliEnd.postValue(true);
        });
        dataThread.start();
    }

    private void stopMic(){
        shortBuffer.position(0);
        if(audioSensor != null){
            audioSensor.stop();
            audioSensor.release();
            audioSensor = null;
        }
    }

    private void calculateDelay(long soundPeak){
        Optional<float[]> aboutMax = accData.stream().max((float[] floats, float[] t1) -> {
                    float val1 = floats[0]*floats[0] + floats[1]*floats[1] + floats[2]*floats[2];
                    float val2 = t1[0] * t1[0] + t1[1] * t1[1] + t1[2]*t1[2];

                    if(val1 > val2)
                        return 1;
                    else if(val1 == val2)
                        return 0;
                    return -1;
                }
        );

        if(aboutMax.isPresent()){
            int imuPeakIdx = accData.indexOf(aboutMax.get());
            long imuPeakTime = imuTimeStamp.get(imuPeakIdx);
            long diff = soundPeak - imuPeakTime;

            Log.d(TAG, "SOUND PEAK Value: " + shortBuffer.get(0));
            Log.d(TAG, "IMU   PEAK Value: " + Arrays.toString(accData.get(imuPeakIdx)));

            Log.d(TAG, "SOUND PEAK Time: " + soundPeak);
            Log.d(TAG, "IMU   PEAK Time: " + imuPeakTime);
            Log.d(TAG, "DIFF: " + diff);

            this.caliData = new CalibrationData(diff);

            shortBuffer.clear();
            imuTimeStamp.clear();
            accData.clear();
            gyroData.clear();
        }
    }

    public CalibrationData getCaliData(){
        return caliData;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch(sensorEvent.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                accData.add(sensorEvent.values);
                imuTimeStamp.add(System.currentTimeMillis());
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroData.add(sensorEvent.values);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void makeBreathData(long soundStartTime, long soundEndTime){
        final long imuStartTime = soundStartTime - caliData.getPeakToPeakDalay();

        long minDiff = 1000000000;
        long val = imuTimeStamp.get(0);
        for(long t : imuTimeStamp){
            if(abs(t - imuStartTime) < minDiff){
                minDiff = abs(t - imuStartTime);
                val = t;
            }
        }

        int imuStartTimeIdx = imuTimeStamp.indexOf(val);

        long endTimeDiff = soundEndTime - imuTimeStamp.get(imuTimeStamp.size()-1);

        breathData = new BreathData(
                accData.subList(imuStartTimeIdx, accData.size()-1)
                , gyroData.subList(imuStartTimeIdx, gyroData.size() - 1)
                , shortBuffer.array()
                );
    }

    static class BreathData{
        List<float[]> acc, gyro;
        short[] sound;
        BreathData(List<float[]> acc, List<float[]>gyro, short[] sound){
            this.acc = acc;
            this.gyro = gyro;
            this.sound = sound;
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            ret.append("accx,");
            for(float[] data : acc){
                ret.append(data[0])
                        .append(",");
            }
            ret.append("\naccy,");
            for(float[] data : acc){
                ret.append(data[1])
                        .append(",");
            }
            ret.append("\naccz,");
            for(float[] data : acc){
                ret.append(data[2])
                        .append(",");
            }
            ret.append("\ngyrox,");
            for(float[] data : gyro){
                ret.append(data[0])
                        .append(",");
            }
            ret.append("\ngyroy,");
            for(float[] data : gyro){
                ret.append(data[1])
                        .append(",");
            }
            ret.append("\ngyroz,");
            for(float[] data : gyro){
                ret.append(data[2])
                        .append(",");
            }

            ret.append(";\nsound,");
            for(short data : sound){
                ret.append(data)
                        .append(",");
            }
            return ret.toString();
        }
    }

    static class CalibrationData{
        long peakToPeakDalay;

        CalibrationData(long peakToPeakDalay){
            this.peakToPeakDalay = peakToPeakDalay;
        }

        long getPeakToPeakDalay(){
            return peakToPeakDalay;
        }

        @NonNull
        @Override
        public String toString() {
            return "" + peakToPeakDalay;
        }

        public static CalibrationData setData(String str){
            if(str == null){
                return null;
            }

            return new CalibrationData(Long.parseLong(str));
        }
    }



}
