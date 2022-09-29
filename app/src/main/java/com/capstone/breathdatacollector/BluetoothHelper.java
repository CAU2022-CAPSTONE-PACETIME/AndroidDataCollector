package com.capstone.breathdatacollector;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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

    private static BluetoothHelper instance;

    private BluetoothHelper() {
        adapter = BluetoothAdapter.getDefaultAdapter();
        profileListener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    Log.d(TAG, "Headset Connected");
                    headset = (BluetoothHeadset) proxy;
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.HEADSET) {
                    Log.d(TAG, "Headset Disconnected");
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

    public static boolean checkBluetoothEnabled(AppCompatActivity activity){
        if(instance == null){
            instance = new BluetoothHelper();
        }
        if (instance.adapter == null) {
            return false;
        }

        ActivityResultLauncher<String> permissionLauncher;

        ActivityResultCallback<Boolean> arc;

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



        if (!instance.adapter.isEnabled()) {
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

            return instance.adapter.isEnabled();
        }
        return true;
    }

    public boolean startBluetoothHeadset(Context context) {
        Log.d(TAG, "start Bluetooth Headset Method");

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                if(AudioManager.SCO_AUDIO_STATE_CONNECTED == state){
                    Log.d(TAG, "onReceive, SCO_AUDIO_CONNECTED");

                    Toast.makeText(context, "Bluetooth Headset Connected", Toast.LENGTH_SHORT).show();
                    adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
                    context.unregisterReceiver(this);
                }
            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));

        Log.d(TAG, "start Bluetooth Headset Method End");

        return true;
    }

    public void disconnectHeadset(){
        adapter.closeProfileProxy(BluetoothProfile.HEADSET, headset);
    }
}
