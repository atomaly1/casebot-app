package lucie.wabartha.suitcaseapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class BluetoothService extends Service {

    BluetoothSPP bluetooth;

    final static String filterConnection = "BluetoothConnectionUpdate";
    final static String keyConnection = "Status";

    final static String filterValue = "BluetoothValueUpdate";
    final static String keyValue = "Value";


    private final IBinder myBinder = new MyLocaleBinder();

    public BluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyLocaleBinder extends Binder {
       public BluetoothService getService(){
            return BluetoothService.this;
        }
    }

    public void onCreate() {
        super.onCreate();

        bluetooth = new BluetoothSPP(this);

        if (!bluetooth.isBluetoothEnabled()) {
            bluetooth.enable();
            msg("Enabling Bluetooth...");
            msg("Bluetooth enabled!");
        } else {
            if (!bluetooth.isServiceAvailable()) {
                bluetooth.setupService();
                bluetooth.startService(BluetoothState.DEVICE_OTHER);
                msg("Bluetooth already enabled!");
            }
        }

        bluetooth.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                sendMessageToActivity("Connected", filterConnection, keyConnection);
            }

            public void onDeviceDisconnected() {
                sendMessageToActivity("Disconnected", filterConnection, keyConnection);

            }

            public void onDeviceConnectionFailed() {
                sendMessageToActivity("Connexion Failed", filterConnection, keyConnection);

            }
        });

        bluetooth.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                sendMessageToActivity(message,filterValue,keyValue);

            }
        });
    }

    private void sendMessageToActivity(String message, String filter, String key){
        Intent i = new Intent(filter);
        i.putExtra(key,message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }


    public void sendData(String data){
        bluetooth.send(data,false);
    }

    public void msg(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void connect(Intent data){
        bluetooth.connect(data);
    }

    public void setupService(){
        bluetooth.setupService();
    }

    public int getServiceState(){
        return bluetooth.getServiceState();
    }

    public void stopService(){
        bluetooth.stopService();
    }
}
