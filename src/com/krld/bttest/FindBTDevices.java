package com.krld.bttest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 4/18/2014.
 */
public class FindBTDevices extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private Button refreshBTDevicesListView;
    private ListView btDevicesListView;
    private List<String> findedBTDevicesMACs;
    private BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_bt_devices);
        initViews();
        refreshBTDevicesListView();
    }

    private void initViews() {
        refreshBTDevicesListView = (Button) findViewById(R.id.refreshDevicesListButton);
        refreshBTDevicesListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshBTDevicesListView();
            }
        });
        btDevicesListView = (ListView) findViewById(R.id.btDevicesListView);
        btDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                startClientActivity(findedBTDevicesMACs.get(index).split("/")[0]);
            }
        });
    }

    private void startClientActivity(String mac) {
        Intent myIntent = new Intent(this, ClientBT.class);
        myIntent.putExtra("mac", mac);
        this.startActivity(myIntent);
    }

    private void refreshBTDevicesListView() {
        showToast("Refreshing BT devices list!");
        btDevicesListView.setAdapter(new ArrayAdapter<String>(FindBTDevices.this, android.R.layout.simple_list_item_1, new ArrayList<String>()));

        initBTAdapter();
        findBTDevices();

        btDevicesListView.setAdapter(new ArrayAdapter<String>(FindBTDevices.this, android.R.layout.simple_list_item_1, findedBTDevicesMACs));
    }

    private void findBTDevices() {
        findedBTDevicesMACs = new ArrayList<String>();
        btAdapter.startDiscovery();
        for (BluetoothDevice btDevice : btAdapter.getBondedDevices()) {
            findedBTDevicesMACs.add(btDevice.getAddress() + "/" + btDevice.getName());
        }
    }

    private void initBTAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter != null) {
            if (btAdapter.isEnabled()) {
                Log.d(Utils.TAG, "Bluetooth on!");
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

        } else {
            Log.e(Utils.TAG, "Bluetooth not finded!");
        }
    }

    private void showToast(String message) {
        try {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}