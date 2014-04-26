package com.krld.bttest.old;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import com.krld.bttest.R;

import java.io.IOException;
import java.util.UUID;

public class ServerActivity extends Activity {

    public BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mmServerSocket;
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.old_server);
        text = (TextView) findViewById(R.id.servertxt);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            text.setText("Does not support bluetooth");
            return;
        }

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        text.setText("Discoverable!!");


        acceptThread();
        new Thread(new Runnable() {
            @Override
            public void run() {
                runLoop();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
     //   getMenuInflater().inflate(R.menu.old_main, menu);
        return true;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.serverb0:




        }
    }

    public void changeT(String str) {
        text.setText(str);
    }

    public void acceptThread() {
        BluetoothServerSocket tmp = null;
        try {
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MYYAPP", MY_UUID_SECURE);

        } catch (IOException e) {
        }
        mmServerSocket = tmp;
    }

    public void runLoop() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                Log.d(BTTestActivity.LOG_TAG, "try accept" );
                socket = mmServerSocket.accept();
                Log.d(BTTestActivity.LOG_TAG, "accepted" );
            } catch (IOException e) {
                Log.d(BTTestActivity.LOG_TAG, "error in loop" + e.getMessage() );
                break;
            }
            if (socket != null) {
                Log.d(BTTestActivity.LOG_TAG, "try close" );
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}