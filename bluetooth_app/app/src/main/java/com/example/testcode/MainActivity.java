package com.example.testcode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static int BLE_PERMISSIONS_REQUEST_CODE = 0x55; // Could be any other positive integer value
    private int permissionsCount;
    private static final String TAG = "MainActivity";
    BluetoothAdapter mBluetoothAdapter;
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //discovery cihaz buldugunda:
            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"onReceive: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"onReceive: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"onReceive: STATE TURNING ON");
                        break;
                }
            }
        }
    };


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //Device found
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                baglandi();
                Toast.makeText(getApplicationContext(), "Bluetooth bağlantısı sağlandı.", Toast.LENGTH_SHORT).show();

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            //Device is about to disconnect

            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            //Device has disconnected
                kesildi();
                Toast.makeText(getApplicationContext(), "Bluetooth bağlantısı kesildi.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void baglandi(){
        Intent baglanti = new Intent(getApplicationContext(),Baglanti.class);
        startActivity(baglanti);
    }
    public void kesildi(){
        Intent mainactivity = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(mainactivity);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);

        checkBlePermissions();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);

        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: enabling/disabling bluetooth.");
                //enableDisableBT();
                onStartBluetoothSettingsClick();
            }
        });//butona basma



    }
    public void onStartBluetoothSettingsClick() {                           //bluetooth ayarları acma
        startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
    }

//----------------------------PERMISSION CHECK--------------------------------
    private String getMissingLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // COARSE is needed for Android 6 to Android 10
            return Manifest.permission.ACCESS_COARSE_LOCATION;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // FINE is needed for Android 10 and above
            return Manifest.permission.ACCESS_FINE_LOCATION;
        }
        // No location permission is needed for Android 6 and below
        return null;
    }

    private boolean hasLocationPermission(String locPermission) {
        if(locPermission == null) return true; // An Android version that doesn't need a location permission
        return ContextCompat.checkSelfPermission(getApplicationContext(), locPermission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private String[] getMissingBlePermissions() {
        String[] missingPermissions = null;

        String locationPermission = getMissingLocationPermission();
        // For Android 12 and above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions = new String[1];
                missingPermissions[0] = Manifest.permission.BLUETOOTH_SCAN;
            }

            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                if (missingPermissions == null) {
                    missingPermissions = new String[1];
                    missingPermissions[0] = Manifest.permission.BLUETOOTH_CONNECT;
                } else {
                    missingPermissions = Arrays.copyOf(missingPermissions, missingPermissions.length + 1);
                    missingPermissions[missingPermissions.length-1] = Manifest.permission.BLUETOOTH_CONNECT;
                }
            }

        }
        else if(!hasLocationPermission(locationPermission)) {
            missingPermissions = new String[1];
            missingPermissions[0] = getMissingLocationPermission();
        }
        return missingPermissions;
    }

    private void checkBlePermissions() {
        String[] missingPermissions = getMissingBlePermissions();
        if(missingPermissions == null || missingPermissions.length == 0) {
            Log.i(TAG, "checkBlePermissions: Permissions is already granted");
            return;
        }

        for(String perm : missingPermissions)
            Log.d(TAG, "checkBlePermissions: missing permissions "+perm);
        permissionsCount = missingPermissions.length;

        requestPermissions(missingPermissions, BLE_PERMISSIONS_REQUEST_CODE);
    }
//----------------------------PERMISSION CHECK--------------------------------




    public void enableDisableBT(){
        if(mBluetoothAdapter == null){        //eğer cihazında bluetooth yoksa
            Log.d(TAG,"enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){              //eğer cihazın bluetooth'u acık degilse
            Log.d(TAG,"enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);   //bluetooth açılsın mı isteği..
            startActivity(enableBTIntent);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);    //IntentFilter ne yapıyor ?
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG,"enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }
}