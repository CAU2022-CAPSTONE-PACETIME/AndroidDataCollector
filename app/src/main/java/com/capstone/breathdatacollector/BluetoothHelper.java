package com.capstone.breathdatacollector;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

public class BluetoothHelper {
    private static final String TAG = "BLUETOOTH_HELPER";

    private BluetoothHeadset headset;
    private BluetoothAdapter adapter;
    private Boolean isEnabled = false;

    private BluetoothProfile.ServiceListener profileListener;
    private BluetoothDevice myDevice;

    public BluetoothHelper(MainActivity activity) {
        Log.d(TAG, "Create Bluetooth Helper");
        adapter = BluetoothAdapter.getDefaultAdapter();

        if(!checkBluetoothEnabled(activity)){
            adapter = null;
        }

        startBluetoothHeadset(activity);
    }

    private boolean checkBluetoothEnabled(AppCompatActivity activity){
        if (adapter == null) {
            return false;
        }

        ActivityResultLauncher<String> permissionLauncher;

        if(activity.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            permissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), (isGranted) -> {
                if (!isGranted) {
                    new AlertDialog.Builder(activity.getApplicationContext())
                            .setTitle("블루투스 접근 권한")
                            .setMessage("앱을 사용하시려면, 블루투스 권한을 허용해 주세요.")
                            .setPositiveButton("확인", (DialogInterface dialog, int which)->{
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            ActivityResultLauncher<Intent> launcher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if(result.getResultCode() != RESULT_OK){

                            new AlertDialog.Builder(activity.getApplicationContext())
                                    .setTitle("블루투스 활성화")
                                    .setMessage("데이터 수집을 위해서, 블루투스를 활성화해 주세요.")
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
            launcher.launch(enableBtIntent);

            return adapter.isEnabled();
        }

        return true;
    }

    @SuppressLint("MissingPermission")
    public boolean startBluetoothHeadset(Context context) {
        Log.d(TAG, "start Bluetooth Headset Method");

        IntentFilter filter = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);

        if(profileListener == null) {
            profileListener = new BluetoothProfile.ServiceListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.HEADSET) {
                        Log.d(TAG, "Headset Connected At Listener");
                        headset = (BluetoothHeadset) proxy;
//
//                        for (BluetoothDevice device : headset.getConnectedDevices()) {
//                            if(device.getName().contains("Watch"))
//                                continue;
//                            if (headset.isAudioConnected(device)) {
//                                myDevice = device;
//                            }
//                            Log.d(TAG, device.getName());
//                            Log.d(TAG, device.getAddress());
//                            Log.d(TAG, "Bluetooth HeadSet Service Connected");
//
//                        }
                    }
                }


                @Override
                public void onServiceDisconnected(int profile) {
                    if (profile == BluetoothProfile.HEADSET) {
                        headset = null;

                        Log.d(TAG, "Headset Disconnected");
                        myDevice = null;
                    }
                }
            };

            adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
        }

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle extras = intent.getExtras();
                int state = extras.getInt(BluetoothProfile.EXTRA_STATE);
                int prevState = extras.getInt(BluetoothProfile.EXTRA_PREVIOUS_STATE);
                BluetoothDevice device = extras.getParcelable(BluetoothDevice.EXTRA_DEVICE);


                if(device != null){
                    if(device.getName().contains("Watch")){
                        Log.i(TAG, "find Watch on Receiver");
                        return;
                    }
                }

                if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                    String stateStr = state == BluetoothHeadset.STATE_AUDIO_CONNECTED ? "AUDIO_CONNECTED"
                            : state == BluetoothHeadset.STATE_AUDIO_CONNECTING ? "AUDIO_CONNECTING"
                            : state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED ? "AUDIO_DISCONNECTED"
                            : "Unknown";
                    String prevStateStr = prevState == BluetoothHeadset.STATE_AUDIO_CONNECTED ? "AUDIO_CONNECTED"
                            : prevState == BluetoothHeadset.STATE_AUDIO_CONNECTING ? "AUDIO_CONNECTING"
                            : prevState == BluetoothHeadset.STATE_AUDIO_DISCONNECTED ? "AUDIO_DISCONNECTED"
                            : "Unknown";
                    assert device != null;
                    Log.d(TAG, "BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED: EXTRA_DEVICE=" + device.getName() + " EXTRA_STATE=" + stateStr + " EXTRA_PREVIOUS_STATE=" + prevStateStr);
                }
                else if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                    String stateStr = state == BluetoothHeadset.STATE_CONNECTED ? "CONNECTED"
                            : state == BluetoothHeadset.STATE_CONNECTING ? "CONNECTING"
                            : state == BluetoothHeadset.STATE_DISCONNECTED ? "DISCONNECTED"
                            : state == BluetoothHeadset.STATE_DISCONNECTING ? "DISCONNECTING"
                            : "Unknown";
                    String prevStateStr = prevState == BluetoothHeadset.STATE_CONNECTED ? "CONNECTED"
                            : prevState == BluetoothHeadset.STATE_CONNECTING ? "CONNECTING"
                            : prevState == BluetoothHeadset.STATE_DISCONNECTED ? "DISCONNECTED"
                            : prevState == BluetoothHeadset.STATE_DISCONNECTING ? "DISCONNECTING"
                            : "Unknown";
                    if(state == BluetoothHeadset.STATE_CONNECTED){
                        myDevice = device;

                        Log.d(TAG, "Bluetooth HeadSet Service Connected At Receiver");
                        Log.d(TAG, device.getName());
                        Log.d(TAG, device.getAddress());


                        Toast.makeText(context, "Bluetooth Headset Connected", Toast.LENGTH_SHORT).show();
                        context.unregisterReceiver(this);
                    }

                    assert device != null;
                    Log.d(TAG, "BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED: EXTRA_DEVICE=" + device.getName() + " EXTRA_STATE=" + stateStr + " EXTRA_PREVIOUS_STATE=" + prevStateStr);
                }

                else if(AudioManager.SCO_AUDIO_STATE_CONNECTED == state){
                    Log.d(TAG, "onReceive, SCO_AUDIO_CONNECTED");
                }
            }
        }, filter);

        if(headset == null){
            Log.d(TAG, "Failed By headset is null");

            Toast.makeText(context, "Headset isn't connected", Toast.LENGTH_LONG).show();

            return false;
        }

        if(headset.getConnectedDevices() == null){
            Log.d(TAG, "Failed By devices list is null");
            return false;
        }

        if(myDevice == null){
            for(BluetoothDevice device : headset.getConnectedDevices()){
                if(device.getName().contains("Watch"))
                    continue;
                myDevice = device;
            }

            if(myDevice == null) {
                Log.d(TAG, "DEVICE is NULL");
                return false;
            }
            return false;
        }

        if(myDevice.getName().contains("Watch")){
            for(BluetoothDevice device : headset.getConnectedDevices()){
                if(device.getName().contains("Watch"))
                    continue;
                myDevice = device;
            }
            if(myDevice.getName().contains("Watch")) {
                Log.d(TAG, "Watch is connected. Please Connect another Device");
                return false;
            }
        }

        Log.d(TAG, "start Bluetooth Headset Method End");
        return true;
    }

    public void disconnectHeadset(){
        adapter.closeProfileProxy(BluetoothProfile.HEADSET, headset);
    }
}
