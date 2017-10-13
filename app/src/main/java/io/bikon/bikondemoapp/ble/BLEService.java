package io.bikon.bikondemoapp.ble;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zoran on 10/12/17.
 */

public class BLEService {
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner btScanner;
    private Activity activity;

    public BLEService(Activity activity) {
        this.activity = activity;
        final BluetoothManager btManager = (BluetoothManager) this.activity.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
    }

    public boolean enableBT() {
        return btAdapter.enable();
    }

    public boolean disableBT() {
        return btAdapter.disable();
    }

    private boolean hasPermissions() {
        if (btAdapter == null || !btAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, 1);
        Log.d("BIKON", "Requested user enables Bluetooth. Try starting the scan again.");
    }

    private boolean hasLocationPermissions() {
        return activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    public void startScan(ScanCallback callback) {
        hasLocationPermissions();
        final List<ScanFilter> filterList = new ArrayList<ScanFilter>();
        final ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
        btScanner = btAdapter.getBluetoothLeScanner();
        btScanner.startScan(callback);

    }
}
