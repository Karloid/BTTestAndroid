package com.krld.bttest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.krld.bttest.old.BTTestActivity;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.LogRecord;

/**
 * Created by Andrey on 4/21/2014.
 */
public class ServerBTActivity extends Activity {
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final boolean SET_DISCOVERABLE = false;
    public static BluetoothSocket pickedSocket;
    private BluetoothAdapter mBluetoothAdapter;
    private TextView status;
    private BluetoothServerSocket serverSocket;
    private RunnerThread runnerThread;
    private List<SocketHandler> socketHandlers;
    private Handler uiHandler;
    private ListView acceptedSockets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_bt);
        initViews();
        initBTServer();
    }

    private void initViews() {
        status = (TextView) findViewById(R.id.statusTextView);
        acceptedSockets = (ListView) findViewById(R.id.serverAcceptedSocketsListView);
        acceptedSockets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                startClientActivity(socketHandlers.get(index));
            }
        });
    }

    private void startClientActivity(SocketHandler socketHandler) {
        Intent myIntent = new Intent(this, ClientBT.class);
        myIntent.putExtra(Utils.PICK_FROM_SERVER_ACTIVITY, "ok");
        pickedSocket = socketHandler.socket;
        this.startActivity(myIntent);
    }

    private void initBTServer() {
        socketHandlers = new ArrayList<SocketHandler>();
        uiHandler = new UIHandler();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            status.setText("Does not support bluetooth");
            return;
        }
        if (SET_DISCOVERABLE) {
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
        status.setText("Discoverable!");

        createServerSocket();

        runnerThread = new RunnerThread();
        runnerThread.start();
    }

    private void createServerSocket() {
        BluetoothServerSocket tmp = null;
        try {
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MYYAPP", MY_UUID_SECURE);

        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = tmp;
    }

    private class RunnerThread extends Thread {
        @Override
        public void run() {
            runLoop();
        }
    }

    public void runLoop() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                Log.d(Utils.TAG, "try accept");
                socket = serverSocket.accept();
                socketHandlers.add(new SocketHandler(socket));
                uiHandler.obtainMessage(0, 0, -1, 0).sendToTarget();
                ;
                Log.d(Utils.TAG, "accepted");
            } catch (IOException e) {
                Log.d(Utils.TAG, "error in loop" + e.getMessage());
                //     break;
            }
        /*    if (socket != null) {
                Log.d(Utils.TAG, "try close" );
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
          //      break;
            }*/
        }
    }

    private class SocketHandler {
        public final BluetoothSocket socket;

        public SocketHandler(BluetoothSocket socket) {
            this.socket = socket;
        }
    }

    private class UIHandler extends Handler {
        public void handleMessage(android.os.Message msg) {
            /*byte[] readBuf = (byte[]) msg.obj;
            String stringInput = new String(readBuf, 0, msg.arg1);
            stringInput = "RECEIVED: " + stringInput;
            addToMessagesList(stringInput);*/
            refreshSocketList();
        }
    }

    private void refreshSocketList() {

        List<String> socketsNames = new ArrayList<String>();
        int i = 0;
        for (SocketHandler socketHandler : socketHandlers) {
            i++;
            socketsNames.add(i + " socket");
        }
        acceptedSockets.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, socketsNames));
    }
}
