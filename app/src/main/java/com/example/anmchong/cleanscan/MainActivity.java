package com.example.anmchong.cleanscan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;

    TextView TV1;
    TextView TV2;
    TextView TV3;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    String StringOfDataIB = "";
    private static final int REQUEST_ENABLE_BT = 1;

    private static final long SCAN_PERIOD = 100000;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // SETTING UP UI VARIABLES AND REFERENCES/////////////////////////////////
        mHandler = new Handler();
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        TV1 = (TextView) findViewById(R.id.textView);
        TV2 = (TextView) findViewById(R.id.textView2);
        TV3 = (TextView) findViewById(R.id.textView3);
        /////////////////////////////////////////////////////////////////////////


        //SETTING UP PERMISSIONS
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {


                    @Override

                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }

                    }


                });

                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.

        // Initializes list view adapter.
        //scanLeDevice(true);
    }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            // User chose not to enable Bluetooth.
            if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

    @Override
    protected void onPause() {
        super.onPause();
//        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    String mys = new String(scanRecord);
                    String MeasuredEddyUID = "xW1I";    //Eddystone device name we want to read
                    String MeasuredIbeacon = "xW1I";    //Ibeacon device name we want to read
                    String FoundMeasureUID = "";        // initializing strings for future comparisons
                    String FoundMeasureURL = "";
                    String FoundMeasureIB = "";

                    // Will create a toast when sees ANY bluetooth signal
                        Toast.makeText(getApplicationContext(), "  RSSI: " + rssi + "dBm", Toast.LENGTH_SHORT).show();
                        StringBuilder sb = new StringBuilder();
                        for (byte b : scanRecord) {
                            sb.append(String.format("%02X ", b));
                        }
                    // Creating the string types we expect to see from our BLE beacon titled xW1I
                        FoundMeasureUID += Character.toString((char) scanRecord[45])+Character.toString((char) scanRecord[46])
                                +Character.toString((char) scanRecord[47])+Character.toString((char) scanRecord[48]);
                        FoundMeasureURL += Character.toString((char) scanRecord[47])+Character.toString((char) scanRecord[48])
                                +Character.toString((char) scanRecord[49])+Character.toString((char) scanRecord[50]);
                        FoundMeasureIB += Character.toString((char) scanRecord[46])+Character.toString((char) scanRecord[47])
                                +Character.toString((char) scanRecord[48])+Character.toString((char) scanRecord[49]);
                        // The above code makes a string to compare to the read string
                        // if the read string matches with one of these, the bluetooth reading belongs
                        // to a beacon and its specific framework

                    // Will output which format we are reading and assign an RSSI value to it
                        if (FoundMeasureUID.compareTo(MeasuredEddyUID) == 0) {
                            TV1.setText(Integer.toString(rssi));
                        }

                        if (FoundMeasureURL.compareTo(MeasuredEddyUID) == 0) {
                            StringOfDataIB +=" " + rssi;
                            TV2.setText(Integer.toString(rssi));
                        }
                        if (FoundMeasureIB.compareTo(MeasuredIbeacon) == 0) {
                            StringOfDataIB +=" " + rssi;
                            TV1.setText(Integer.toString(rssi));
                        }

                    if ((FoundMeasureUID.compareTo(MeasuredEddyUID) == 0) |
                            (FoundMeasureURL.compareTo(MeasuredEddyUID) == 0) |
                            (FoundMeasureIB.compareTo(MeasuredIbeacon) == 0) ) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                }
            };

// Start and stop control of the update flag, if scanLeDevice is called with true, we will scan
    public void startUpdatesButtonHandler(View view) {
        scanLeDevice(true);
    }

    public void stopUpdatesButtonHandler(View view) {
        scanLeDevice(false);
    }
    /////////////////////////////////////////////////////////////////////////

}