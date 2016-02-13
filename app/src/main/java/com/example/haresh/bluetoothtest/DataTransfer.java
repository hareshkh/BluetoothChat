package com.example.haresh.bluetoothtest;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class DataTransfer extends AppCompatActivity {

    String TAG = "DataTransfer";

    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;
    Button bt1;
    EditText inputMessage;
    TextView messages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transfer);
        bluetoothDevice = BluetoothActivity.mBluetoothDevice;
        bluetoothSocket = BluetoothActivity.mBluetoothSocket;
        final ConnectedThread ct = new ConnectedThread(bluetoothSocket);
        ct.start();

        messages = (TextView) findViewById(R.id.textView);
        inputMessage = (EditText) findViewById(R.id.editText);

        bt1 = (Button) findViewById(R.id.send);
        bt1.setOnClickListener(
                new View.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(View v) {
                        if (!Objects.equals(inputMessage.getText().toString(), "")){
                            Log.d(TAG, "Clicked");
                            String msg = inputMessage.getText().toString().toUpperCase();
                            byte[] bytes = msg.getBytes();
                            ct.write(bytes);
                        }
                    }
                }
        );
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    final String readMessage = new String(buffer, 0, bytes);
                    Log.i(TAG,"Listening "+readMessage);
                    runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if(readMessage!=null)
                                        messages.setText(messages.getText()+"\n"+readMessage);
                                }
                            }
                    );
                } catch (Exception e) {
                    Log.e(TAG, "disconnected", e);
                    //connectionLost();
                    //break;
                }
            }
        }
        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                Log.d(TAG, "Writing");
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                /*mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();*/
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
