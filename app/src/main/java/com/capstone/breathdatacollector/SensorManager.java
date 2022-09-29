package com.capstone.breathdatacollector;

import android.content.Context;
import android.hardware.Sensor;

public class SensorManager {

    private Context context;

    private static SensorManager instance;
    private Sensor accSensor;

    private SensorManager(){}

    public static SensorManager getInstance(){
        if(instance == null){
            instance = new SensorManager();
        }
        return instance;
    }

    public void setContext(Context context){
        this.context = context;

        setSensors();
    }

    private void setSensors(){
        android.hardware.SensorManager sensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR); // TODO: add permission ACTIVITY_RECOGNITION
    }
}
