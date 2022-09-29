package com.capstone.breathdatacollector;

import android.content.Context;
import android.hardware.Sensor;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.time.LocalDateTime;

public class SensorManager {
    private static final String TAG = "SENSOR_MANAGER";
    private Context context;

    private static SensorManager instance;
    private Sensor accSensor, stepSensor;

    private AudioRecord audioSensor;

    private BluetoothHelper bluetoothHelper;

    private SensorManager(){
        bluetoothHelper = BluetoothHelper.getInstance();
    }

    public static SensorManager getInstance(){
        if(instance == null){
            instance = new SensorManager();
        }
        return instance;
    }

    public void setContext(Context context){
        this.context = context;

        setSensors();
        setMic();
    }

    private void setSensors(){
        android.hardware.SensorManager sensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

//        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR); // TODO: add permission ACTIVITY_RECOGNITION
    }

    private void setMic(){
        AudioManager audioManager = (AudioManager)this.context.getSystemService(Context.AUDIO_SERVICE);

        if(bluetoothHelper.startBluetoothHeadset(context)){
            audioManager.startBluetoothSco();
        }
    }
}
