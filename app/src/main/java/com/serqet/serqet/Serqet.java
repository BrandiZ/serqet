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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.UUID;


public class Serqet extends Activity {

    private GraphView graph;

    DataPoint[] points;

    LineGraphSeries wave;
    LineGraphSeries trigger;

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
    private double VOLTAGE_RANGE_MIN = 0.03;
    private double VOLTAGE_OFFSET_MAX = 15;
    private double VOLTAGE_OFFSET_MIN = -15;
    private double TIME_RANGE_MAX = 2;
    private double TIME_RANGE_MIN = 0.00002;
    private double TIME_OFFSET_MAX = 1;
    private double TIME_OFFSET_MIN = 0;
    private double TRIGGER_LEVEL_MAX = 15;
    private double TRIGGER_LEVEL_MIN = -15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serqet);

        graph = (GraphView) findViewById(R.id.graph);

        wave = new LineGraphSeries<DataPoint>();
        trigger = new LineGraphSeries<DataPoint>();
        trigger.setColor(0xffff0000);

        graph.addSeries(wave);
        graph.addSeries(trigger);

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
                if(seekBar.equals(triggerLevelBar)){
                    updateTrigger();
                }
            }
        };

        averagingBar.setMax(AVERAGING_MODES.length - 1);

        voltageRangeBar.setOnSeekBarChangeListener(seekBarListener);
        voltageOffsetBar.setOnSeekBarChangeListener(seekBarListener);
        timeRangeBar.setOnSeekBarChangeListener(seekBarListener);
        timeOffsetBar.setOnSeekBarChangeListener(seekBarListener);

        triggerLevelBar.setOnSeekBarChangeListener(seekBarListener);
        averagingBar.setOnSeekBarChangeListener(seekBarListener);

        updateSettings();
        updateTrigger();
        updateWave();
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
        voltageRangeValue = getRangeValueFromBar(voltageRangeBar, VOLTAGE_RANGE_MAX, VOLTAGE_RANGE_MIN);
        timeRangeValue = getRangeValueFromBar(timeRangeBar, TIME_RANGE_MAX, TIME_RANGE_MIN);

        double voltageScaleFactor = voltageRangeValue / VOLTAGE_RANGE_MAX;
        double timeScaleFactor = timeRangeValue / TIME_RANGE_MAX;

        voltageOffsetValue = getValueFromBar(
                voltageOffsetBar,
                voltageScaleFactor * VOLTAGE_OFFSET_MAX,
                voltageScaleFactor * VOLTAGE_OFFSET_MIN
        );
        timeOffsetValue = getValueFromBar(
                timeOffsetBar,
                timeScaleFactor * TIME_OFFSET_MAX,
                timeScaleFactor * TIME_OFFSET_MIN
        );
        triggerLevelValue = getValueFromBar(
                triggerLevelBar,
                voltageScaleFactor * TRIGGER_LEVEL_MAX,
                voltageScaleFactor * TRIGGER_LEVEL_MIN
        );
        averagingValue = AVERAGING_MODES[averagingBar.getProgress()];

        voltageRangeValueText.setText(formatNumber(voltageRangeValue) + "V");
        voltageOffsetValueText.setText(formatNumber(voltageOffsetValue) + "V");
        timeRangeValueText.setText(formatNumber(timeRangeValue) + "s");
        timeOffsetValueText.setText(formatNumber(timeOffsetValue) + "s");

        triggerLevelValueText.setText(formatNumber(triggerLevelValue) + "V");
        averagingValueText.setText(averagingValue == 1 ? "Off" : averagingValue + "x");

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(timeOffsetValue + timeRangeValue);
        graph.getViewport().setMinX(timeOffsetValue);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxY(voltageOffsetValue + voltageRangeValue / 2);
        graph.getViewport().setMinY(voltageOffsetValue - voltageRangeValue / 2);

        graph.onDataChanged(true, false);
    }

    public void updateTrigger(){
        trigger.resetData(new DataPoint[]{
                new DataPoint(-1000000, triggerLevelValue),
                new DataPoint(1000000, triggerLevelValue)
        });
    }

    public void updateWave(){
        points = new DataPoint[1024];

        for (int i = 0; i < 1024; i++){
            points[i] = new DataPoint(i/1000.0, Math.sin(i*2*Math.PI/50));
        }

        wave.resetData(points);
    }

    public double getValueFromBar(SeekBar bar, double max, double min){
        float fraction = ((float)bar.getProgress()) / bar.getMax();
        return (fraction) * (max - min) + min;
    }

    public double getRangeValueFromBar(SeekBar bar, double max, double min){
        float fraction = ((float)bar.getProgress()) / bar.getMax();
        return Math.exp((fraction) * (Math.log(max) - Math.log(min)) + Math.log(min));
    }

    public String formatNumber(double num){
        if(Math.abs(num) >= 1){
            return String.format("%.3f", num);
        }
        if(Math.abs(num) >= 0.001){
            return String.format("%.3fm", num * 1000);
        }
        return String.format("%.3fμ", num * 1000000);
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
