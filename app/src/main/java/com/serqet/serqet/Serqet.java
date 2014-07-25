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
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class Serqet extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serqet);

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
