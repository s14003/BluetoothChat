package jp.ac.it_college.std.s14003.android.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.UUID;

public class BTCommunicator extends Thread {
    public static final int DISCONNECT = 99;

    public static final int DISPLAY_TOAST = 1000;
    public static final int STATE_CONNECTED = 1001;
    public static final int STATE_CONNECT_ERROR = 1002;
    public static final int STATE_RECEIVE_ERROR = 1004;
    public static final int STATE_SEND_ERROR = 1005;
    public static final int NO_DELAY = 0;

    private static final UUID SERIAL_PORT_SERVICE_CLASS_UUID = UUID.fromString(
            "00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter btAdapter;
    private BluetoothSocket nxtBTSocket = null;
    private DataOutputStream nxtDos = null;
    private DataInputStream nxtDin = null;
    private boolean connected = false;

    private Handler handler;
    private String mMacAddress;
    private MainActivity mainActivity;

    public BTCommunicator(MainActivity mainActivity,
                          Handler myHandler, BluetoothAdapter defaultAdapter) {
        this.mainActivity = mainActivity;
        this.handler = myHandler;
        this.btAdapter = defaultAdapter;
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void run() {
        createConnection();
    }

    private void createConnection() {
        try {
            BluetoothSocket nxtBTSocketTEMPORARY;
            BluetoothDevice nxtDevice;
            nxtDevice = btAdapter.getRemoteDevice(mMacAddress);

            if (nxtDevice == null) {
                sendToast(mainActivity.getResources().getString(R.string.none_paired));
                sendState(STATE_CONNECT_ERROR);
                return;
            }
            nxtBTSocketTEMPORARY = nxtDevice.createRfcommSocketToServiceRecord(
                    SERIAL_PORT_SERVICE_CLASS_UUID);
            nxtBTSocketTEMPORARY.connect();
            nxtBTSocket = nxtBTSocketTEMPORARY;

            nxtDin = new DataInputStream(nxtBTSocket.getInputStream());
            nxtDos = new DataOutputStream(nxtBTSocket.getOutputStream());

            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("BTCommunicator", "error createNXTConnection()", e);
            if (mainActivity.newDevice) {
                sendToast(mainActivity.getResources().getString(R.string.pairing_message));
                sendState(STATE_CONNECT_ERROR);
            } else {
                sendState(STATE_CONNECT_ERROR);
            }
            return;
        }
        sendState(STATE_CONNECT_ERROR);
    }

    private void destoryNXTconnection() {
        try {
            if (nxtBTSocket != null) {
                connected = false;
                nxtBTSocket.close();
                nxtBTSocket = null;
            }
            nxtDin = null;
            nxtDos = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] readData() {
        byte[] buffer = new byte[2];
        byte[] result;
        int numBytes;
        try {

            //noinspection ResultOfMethodCallIgnored
            nxtDin.read(buffer, 0, buffer.length);
            numBytes = (int) buffer[0] + (buffer[1] << 8);

            result = new byte[numBytes];
            //noinspection ResultOfMethodCallIgnored
            nxtDin.read(result, 0, numBytes);

        } catch (IOException e) {
            Log.e("read_data", "Read failed.", e);
            throw new RuntimeException(e);
        }
        Log.v("read_data", "Read:" + Arrays.toString(result));

        return result;
    }

    private boolean sendMessage(byte[] message) {
        if (nxtDos == null) {
            return false;
        }

        int bodyLength = message.length;

        byte[] header = {
                (byte) (bodyLength & 0xff), (byte) ((bodyLength >>> 8) & 0xff)
        };
        try {
            nxtDos.write(header);
            nxtDos.write(message);
            nxtDos.flush();
        } catch (IOException e) {
            sendState(STATE_SEND_ERROR);
            return false;
        }
        return true;
    }

    private String byteToStr(byte[] mess) {
        StringBuilder stringBuffer = new StringBuilder();
        for (byte mes : mess) {
            stringBuffer.append(String.format("%02x", (mes)));
        }
        return stringBuffer.toString();
    }

    private void waitSomeTime(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //必ずなにか入れる
            Log.e("waitSomeTime", e.getMessage(), e);
        }
    }

    private void sendToast(String toastText) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", DISPLAY_TOAST);
        myBundle.putString("toastText", toastText);
        sendBundle(myBundle);
    }

    private void sendState(int message) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        sendBundle(myBundle);
    }

    private void sendState(int message, int value) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        myBundle.putInt("value1", value);
        sendBundle(myBundle);
    }

    private void sendState(int message, float value) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        myBundle.putFloat("value1", value);
    }

    private void sendBundle(Bundle myBundle) {
        Message myMessage = handler.obtainMessage();
        myMessage.setData(myBundle);
        handler.sendMessage(myMessage);
    }

    public void setMacAddress(String mMacAddress) {
        this.mMacAddress = mMacAddress;
    }

    public static class BTCommunicatorHandler extends Handler {
        private final WeakReference<BTCommunicator> reference;

        public BTCommunicatorHandler(BTCommunicator communicator) {
            reference = new WeakReference<>(communicator);
        }
        @Override
        public void handleMessage(Message myMessage) {
            BTCommunicator communicator = reference.get();
            if (communicator == null) {
                return;
            }
            switch (myMessage.getData().getInt("message")) {
                case DISCONNECT:
                    communicator.destoryNXTconnection();
                    break;

            }
        }
    }
}
