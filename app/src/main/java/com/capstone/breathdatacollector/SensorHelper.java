package com.capstone.breathdatacollector;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    private ActivityResultLauncher<String> permissionLauncher;

    public SensorHelper(MainActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        bluetoothHelper = BluetoothHelper.getInstance();
        permissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), (isGranted) -> {
            if (!isGranted) {
                new AlertDialog.Builder(activity.getApplicationContext())
                        .setTitle("녹음 권한")
                        .setMessage("앱을 사용하시려면, 녹음 권한을 허용해 주세요.")
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

            Log.i("AUDIO_INFO", "Check Self Permission");

            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                return;
            }
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

    public String getData() {
        return "";
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

            calculateDelay(start, end, soundPeakTime);
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

    private void calculateDelay(long soundStart, long soundEnd, long soundPeak){
        int imuPeakIdx = accData.indexOf(accData.stream().max(new Comparator<float[]>() {
            @Override
            public int compare(float[] floats, float[] t1) {
                float val1 = floats[0]*floats[0] + floats[1]*floats[1] + floats[2]*floats[2];
                float val2 = t1[0] * t1[0] + t1[1] * t1[1] + t1[2]*t1[2];

                if(val1 > val2)
                    return 1;
                else if(val1 == val2)
                    return 0;
                return -1;
            }
        }).get());

        long imuPeakTime = imuTimeStamp.get(imuPeakIdx);

        long diff = imuPeakTime - soundPeak;

        Log.d(TAG, "DIFF: " + diff);
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
}
