package com.capstone.breathdatacollector;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
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

    private static BluetoothHelper instance;

    private BluetoothHelper() {
        adapter = BluetoothAdapter.getDefaultAdapter();
        profileListener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    headset = (BluetoothHeadset) proxy;
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.HEADSET) {
                    headset = null;
                }
            }
        };
    }

    public static BluetoothHelper getInstance(){
        if(instance == null){
            instance = new BluetoothHelper();
        }
        return instance;
    }

    public boolean checkBluetoothEnabled(AppCompatActivity activity){
        if (adapter == null) {
            return false;
        }
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            ActivityResultLauncher<Intent> launcher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {});

            launcher.launch(enableBtIntent);
            return adapter.isEnabled();
        }
        return true;
    }

    public boolean startBluetoothHeadset(Context context) {

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                if(AudioManager.SCO_AUDIO_STATE_CONNECTED == state){
                    context.unregisterReceiver(this);

                    Toast.makeText(context, "Bluetooth Headset Connected", Toast.LENGTH_SHORT).show();

                    adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
                }
            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));

        return true;
    }

    public void disconnectHeadset(){
        adapter.closeProfileProxy(BluetoothProfile.HEADSET, headset);
    }
}
