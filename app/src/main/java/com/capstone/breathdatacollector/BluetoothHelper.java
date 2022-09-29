package com.capstone.breathdatacollector;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class BluetoothHelper {

    private BluetoothHeadset headset;
    private BluetoothAdapter adapter;

    private BluetoothProfile.ServiceListener profileListener;

    public BluetoothHelper() {
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

    public boolean startBluetoothHeadset(Context context) {
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            return false;
        }

        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return false;
            }
            context.startActivity(enableBtIntent);
        }

        adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
        return true;
    }

    public void disconnectHeadset(){
        adapter.closeProfileProxy(BluetoothProfile.HEADSET, headset);;
    }
}
