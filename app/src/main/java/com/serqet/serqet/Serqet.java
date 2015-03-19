package com.serqet.serqet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.UUID;


public class Serqet extends Activity {

    private ToggleButton couplingButton;
    private ToggleButton triggerEdgeButton;

    private SeekBar voltageRangeBar;
    private SeekBar voltageOffsetBar;
    private SeekBar timeRangeBar;
    private SeekBar timeOffsetBar;

    private SeekBar triggerLevelBar;
    private SeekBar averagingBar;

    private TextView voltageRangeValueText;
    private TextView voltageOffsetValueText;
    private TextView timeRangeValueText;
    private TextView timeOffsetValueText;
    private TextView triggerLevelValueText;
    private TextView averagingValueText;

    private double voltageRangeValue;
    private double voltageOffsetValue;
    private double timeRangeValue;
    private double timeOffsetValue;
    private double triggerLevelValue;
    private int averagingValue;

    private int[] AVERAGING_MODES = {1, 2, 4, 8};
    private double VOLTAGE_RANGE_MAX = 30;
    private double VOLTAGE_RANGE_MIN = 0;
    private double VOLTAGE_OFFSET_MAX = 15;
    private double VOLTAGE_OFFSET_MIN = -15;
    private double TIME_RANGE_MAX = 10;
    private double TIME_RANGE_MIN = 0.001;
    private double TIME_OFFSET_MAX = 10;
    private double TIME_OFFSET_MIN = 0;
    private double TRIGGER_LEVEL_MAX = 15;
    private double TRIGGER_LEVEL_MIN = -15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serqet);

        couplingButton = (ToggleButton) findViewById(R.id.couplingButton);
        triggerEdgeButton = (ToggleButton) findViewById(R.id.triggerEdgeButton);

        voltageRangeBar = (SeekBar) findViewById(R.id.voltageRangeSeekBar);
        voltageOffsetBar = (SeekBar) findViewById(R.id.voltageOffsetSeekBar);
        timeRangeBar = (SeekBar) findViewById(R.id.timeRangeSeekBar);
        timeOffsetBar = (SeekBar) findViewById(R.id.timeOffsetSeekBar);
        triggerLevelBar = (SeekBar) findViewById(R.id.triggerLevelSeekBar);
        averagingBar = (SeekBar) findViewById(R.id.averagingSeekBar);

        voltageRangeValueText = (TextView) findViewById(R.id.voltageRangeValue);
        voltageOffsetValueText = (TextView) findViewById(R.id.voltageOffsetValue);
        timeRangeValueText = (TextView) findViewById(R.id.timeRangeValue);
        timeOffsetValueText = (TextView) findViewById(R.id.timeOffsetValue);
        triggerLevelValueText = (TextView) findViewById(R.id.triggerLevelValue);
        averagingValueText = (TextView) findViewById(R.id.averagingValue);

        SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSettings();
            }
        };

        averagingBar.setMax(AVERAGING_MODES.length - 1);

        voltageRangeBar.setOnSeekBarChangeListener(seekBarListener);
        voltageOffsetBar.setOnSeekBarChangeListener(seekBarListener);
        timeRangeBar.setOnSeekBarChangeListener(seekBarListener);
        timeOffsetBar.setOnSeekBarChangeListener(seekBarListener);

        triggerLevelBar.setOnSeekBarChangeListener(seekBarListener);
        averagingBar.setOnSeekBarChangeListener(seekBarListener);

    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.serqet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCouplingButtonClick(View view) {
        updateSettings();
    }

    public void onTriggerEdgeButtonClick(View view) {
        updateSettings();
    }

    public void updateSettings(){
        voltageRangeValue = getValueFromBar(voltageRangeBar, VOLTAGE_RANGE_MAX, VOLTAGE_RANGE_MIN);
        voltageOffsetValue = getValueFromBar(voltageOffsetBar, VOLTAGE_OFFSET_MAX, VOLTAGE_OFFSET_MIN);
        timeRangeValue = getValueFromBar(timeRangeBar, TIME_RANGE_MAX, TIME_RANGE_MIN);
        timeOffsetValue = getValueFromBar(timeOffsetBar, TIME_OFFSET_MAX, TIME_OFFSET_MIN);
        triggerLevelValue = getValueFromBar(triggerLevelBar, TRIGGER_LEVEL_MAX, TRIGGER_LEVEL_MIN);
        averagingValue = AVERAGING_MODES[averagingBar.getProgress()];

        voltageRangeValueText.setText(voltageRangeValue + "V");
        voltageOffsetValueText.setText(voltageOffsetValue + "V");
        timeRangeValueText.setText(timeRangeValue + "s");
        timeOffsetValueText.setText(timeOffsetValue + "s");


        System.out.println("update");
    }

    public double getValueFromBar(SeekBar bar, double max, double min){
        return (((float)bar.getProgress())/bar.getMax()) * (max - min) + min;
    }

    public void manageBluetooth (View view ) {
        BluetoothSocket mmSocket;

        TextView textView = new TextView(this);
        textView.setTextSize(40);
        // Set the text view as the activity layout
        setContentView(textView);


        InputStream mmInStream=null;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        Set<BluetoothDevice> pairedDevices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices(); // get list of paired devices
        BluetoothDevice mmDevice = null;

        for (BluetoothDevice device : pairedDevices) {
            String name = device.getName();
            if (name.contains("HC-05")) {
                mmDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getAddress());
                Toast.makeText(getApplicationContext(), "Retrieved paired device",
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard SerialPortService ID
        try {
            mBluetoothAdapter.cancelDiscovery();
            mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            try {
                mmSocket.connect();
                try {
                    mmInStream = mmSocket.getInputStream();

                    Toast.makeText(getApplicationContext(), "Connected",
                            Toast.LENGTH_SHORT).show();


                } catch (IOException e_getin) {
                }
            } catch (IOException econnect) {
            }
        } catch (IOException ecreate) {
        }

        byte[] buffer = new byte[1024];  // buffer store for the stream
        char[] cBuffer = new char[1024];  // buffer store for the stream


        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
       // while (true)
        try {
            // Read from the InputStream
//            bytes = mmInStream.read(buffer);
            InputStreamReader mInputStreamReader = new InputStreamReader(mmInStream);
            int num = mInputStreamReader.read (cBuffer, 0, 40);


            // Send the obtained bytes to the UI activity

            Log.i("Serqet", "Serqet.getView() - got bytes " + num);

            // Create the text view

            textView.append(new String(cBuffer));



        } catch (IOException e) {
        }


    }

}
