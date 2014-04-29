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
    private static final byte TERMINATOR_BYTE = 0;
    private String mac;
    private BluetoothDevice btDevice;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private SocketHandlerThread socketHandler;
    private EditText messageEditText;
    private Button send;

    private List<String> messages;
    private ListView messagesLogListView;
    private InputDataHandler inputDataHandler;
    private TextView statusTextView;
    private List<BTMessage> messagesObj;
    private Button disconnectButton;
    private Button debugButton;
    private CheckBox showBytesCheckBox;
    private CheckBox sendTerminatorByteCheckBox;

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
        messagesObj = new ArrayList<BTMessage>();
        inputDataHandler = new InputDataHandler();
    }

    private boolean connectSocket() {
        Log.d(Utils.TAG, "***Connecting...***");
        try {
            if (!btSocket.isConnected())
                btSocket.connect();
            Log.d(Utils.TAG, "***Соединение успешно установлено***");
            socketHandler = new SocketHandlerThread(btSocket);
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
                if (message.isEmpty() || btSocket == null || !btSocket.isConnected()) {
                    return;
                }
                socketHandler.sendData(message);
                messageEditText.setText("");
            }
        });
        messagesLogListView = (ListView) findViewById(R.id.clientBtMessagesListView);
        disconnectButton = (Button) findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectSocket();
            }
        });
        debugButton = (Button) findViewById(R.id.debugButton);
        debugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iterateSvetodiodZozulya();
            }
        });
        showBytesCheckBox = (CheckBox) findViewById(R.id.showBytesCheckBox);
        showBytesCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                refreshMessageLogListView();
            }
        });
        sendTerminatorByteCheckBox = (CheckBox) findViewById(R.id.sendTerminatorByteCheckBox);
    }

    private void iterateSvetodiodZozulya() {
        int count = 5;
        long delay;
        int type = 2;
        try {
            delay = Integer.valueOf(messageEditText.getText().toString());
        } catch (Exception e) {
            delay = 100;
        }
        for (int i = 0; i < count; i++) {
            if (type == 1)
                for (int index = 1; i <= 8; index++) {
                    socketHandler.sendData(index + "");
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            if (type == 2) {
                int index = 1;
                boolean top = true;
                while (true) {
                    if (index == 0) {
                        break;
                    }
                    if (index == 9) {
                        top = !top;
                        index--;
                    }
                    socketHandler.sendData(index + "");
                    if (top) {
                        index++;
                    } else {
                        index--;
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void disconnectSocket() {
        if (btSocket != null && btSocket.isConnected()) {
            try {
                btSocket.close();
                statusTextView.setText("Disconnected!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketHandlerThread extends Thread {
        private final BluetoothSocket socket;
        private final OutputStream outStream;
        private final InputStream inpStream;

        public SocketHandlerThread(BluetoothSocket btSocket) {
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

        public void sendData(String messageString) {
            byte[] msgBuffer = messageString.getBytes();
            if (sendTerminatorByteCheckBox.isChecked()) {
                byte[] tmpBuffer = new byte[msgBuffer.length + 1];
                System.arraycopy(msgBuffer, 0, tmpBuffer, 0, msgBuffer.length);
                tmpBuffer[msgBuffer.length + 1 - 1] = TERMINATOR_BYTE;
                msgBuffer = tmpBuffer;
            }
            BTMessage message = new BTMessage(messageString, msgBuffer, BTMessage.Types.SEND);
            addToMessagesList(message);
            Log.d(Utils.TAG, "Send data: " + messageString + "***");

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
            byte[] bytesToSave = new byte[msg.arg1];
            System.arraycopy(readBuf, 0, bytesToSave, 0, msg.arg1);
            BTMessage message = new BTMessage(stringInput, bytesToSave, BTMessage.Types.RECEIVED);
            addToMessagesList(message);
        }
    }

    private void addToMessagesList(BTMessage message) {
        messagesObj.add(message);

        refreshMessageLogListView();
    }

    private void refreshMessageLogListView() {
        messages = new ArrayList<String>();
        for (BTMessage btMessage : messagesObj) {
            messages.add(btMessage.getLogString(showBytesCheckBox.isChecked()));
        }
        ArrayList<String> messagesCloned = (ArrayList<String>) ((ArrayList<String>) messages).clone();
        try {
            clone();
        } catch (CloneNotSupportedException e) {
        }
        Collections.reverse(messagesCloned);
        messagesLogListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messagesCloned));
    }

}
