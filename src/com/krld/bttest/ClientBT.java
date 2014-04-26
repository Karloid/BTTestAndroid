package com.krld.bttest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by Andrey on 4/18/2014.
 */
public class ClientBT extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String mac;
    private BluetoothDevice btDevice;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private SocketHandler socketHandler;
    private EditText messageEditText;
    private Button send;

    private List<String> messages;
    private ListView messagesListView;
    private InputDataHandler inputDataHandler;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_bt);
        initViews();

        mac = getIntent().getStringExtra("mac");

        if (mac != null && !mac.isEmpty()) {
            statusTextView.setText("test");

            //   statusTextView.draw
            initBTStuff();
        } else {
            btSocket = ServerBTActivity.pickedSocket;
            connectSocket();
            initInputHandler();
            statusTextView.setText("Connected!");
        }
        statusTextView.invalidate();
    }

    @SuppressLint("ResourceAsColor")
    private void initBTStuff() {
        initInputHandler();
        statusTextView.setText("Init BT adapter");
        if (!initBTAdapter()) {
            statusTextView.setText("FAIL: Init BT adapter");
            statusTextView.postInvalidate();
            return;
        }
        statusTextView.setText("Create BT socket");
        if (!createSocket(mac)) {
            statusTextView.setText("FAIL: Create BT socket");
            statusTextView.postInvalidate();
            return;
        }
        statusTextView.setText("Connect socket");
        if (!connectSocket()) {
            statusTextView.setText("FAIL: Connect socket");
            return;
        }
        statusTextView.setText("Connected!");


    }

    private void initInputHandler() {
        messages = new ArrayList<String>();
        inputDataHandler = new InputDataHandler();
    }

    private boolean connectSocket() {
        Log.d(Utils.TAG, "***Соединяемся...***");
        try {
            if (!btSocket.isConnected())
                btSocket.connect();
            Log.d(Utils.TAG, "***Соединение успешно установлено***");
            socketHandler = new SocketHandler(btSocket);
            socketHandler.start();
            return true;
        } catch (IOException e) {
            Log.d(Utils.TAG, "***Соединение не получилось установить*** " + e.getMessage());
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.d(Utils.TAG, "error closing socket");
            }
        }

        return false;
    }

    private boolean initBTAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter != null) {
            if (btAdapter.isEnabled()) {
                Log.d(Utils.TAG, "Bluetooth on!");
                return true;
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

        } else {
            Log.e(Utils.TAG, "Bluetooth not finded!");
        }
        return false;
    }

    private boolean createSocket(String mac) {
        btDevice = btAdapter.getRemoteDevice(mac);
        Log.d(Utils.TAG, " get bt device: " + btDevice.getName());

        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d(Utils.TAG, "created socket!");
            return true;
        } catch (IOException e) {
            Log.e(Utils.TAG, "error create socket");
        }
        return false;
    }

    private void initViews() {
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        messageEditText = (EditText) findViewById(R.id.clientBTEditText);
        send = (Button) findViewById(R.id.clientSendButton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEditText.getText().toString();
                if (message.isEmpty()) {
                    return;
                }
                socketHandler.sendData(message);
                messageEditText.setText("");
            }
        });
        messagesListView = (ListView) findViewById(R.id.clientBtMessagesListView);
    }

    private class SocketHandler extends Thread {
        private final BluetoothSocket socket;
        private final OutputStream outStream;
        private final InputStream inpStream;

        public SocketHandler(BluetoothSocket btSocket) {
            this.socket = btSocket;
            OutputStream tmpOut = null;
            InputStream tmpIn = null;
            try {
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            outStream = tmpOut;
            inpStream = tmpIn;
        }

        @Override
        public void run() {
            byte[] readedBytes = new byte[1024];
            int countBytes;

            while (true) {
                try {
                    countBytes = inpStream.read(readedBytes);
                    Log.d(Utils.TAG, "data from socket " + countBytes);
                    inputDataHandler.obtainMessage(1, countBytes, -1, readedBytes).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

            }
        }

        public void sendData(String message) {
            addToMessagesList("SEND: " + message);
            byte[] msgBuffer = message.getBytes();
            Log.d(Utils.TAG, "Send data: " + message + "***");

            try {
                outStream.write(msgBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class InputDataHandler extends Handler {
        public void handleMessage(android.os.Message msg) {
            byte[] readBuf = (byte[]) msg.obj;
            String stringInput = new String(readBuf, 0, msg.arg1);
            stringInput = "RECEIVED: " + stringInput;
            addToMessagesList(stringInput);
        }
    }

    private void addToMessagesList(String string) {
        messages.add(string);
        ArrayList<String> messagesCloned = (ArrayList<String>) ((ArrayList<String>) messages).clone();
        try {
            clone();
        } catch (CloneNotSupportedException e) {
        }
        Collections.reverse(messagesCloned);
        messagesListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messagesCloned));
    }

}
