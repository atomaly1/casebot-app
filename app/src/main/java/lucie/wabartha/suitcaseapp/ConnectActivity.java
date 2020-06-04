package lucie.wabartha.suitcaseapp;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;

import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class ConnectActivity extends AppCompatActivity {

    BluetoothService btService;
    Boolean isBound = false;
    int image;

    final static String filterConnection = "BluetoothConnectionUpdate";
    final static String keyConnection = "Status";
    final static String OFF = "2";

    private SoundPool soundPool;
    private int connectedSound, disconnectedSound, failureSound;

    ImageButton connect;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        connect = findViewById(R.id.connectButton);
        textView = findViewById(R.id.connectTv);

        if (savedInstanceState == null){
            image = R.drawable.ring_blue;
        }else {
            image = savedInstanceState.getInt("Image",R.drawable.ring_blue);
        }

        Intent mIntent = new Intent(this, BluetoothService.class);
        bindService(mIntent,bluetoothConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(filterConnection));

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        connectedSound = soundPool.load(this, R.raw.connected,1);
        disconnectedSound = soundPool.load(this, R.raw.disconnected,1);
        failureSound = soundPool.load(this, R.raw.failure,1);

        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(btService.getServiceState() == BluetoothState.STATE_CONNECTED){
                    onResume();

                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String lastState = textView.getText().toString();
        savedInstanceState.putString("State",lastState);
        savedInstanceState.putInt("Image",image);
    }

    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String lastState = savedInstanceState.getString("State");
        textView.setText(lastState);
        int image = savedInstanceState.getInt("Image");
        connect.setImageResource(image);
    }

    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        btService.sendData(OFF);
        btService.stopService();
        soundPool.release();
        soundPool = null;
        super.onDestroy();
    }


    protected void onResume() {

        if(isBound){
            if(btService.getServiceState() == BluetoothState.STATE_CONNECTED){
                Intent intent = new Intent(ConnectActivity.this, RunActivity.class);
                startActivity(intent);
            }
        }

        //msg("onResume");
        super.onResume();
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(keyConnection);
            if ("Connected".equals(message)) {
                soundPool.play(connectedSound,1,1,0,0,1);
                textView.setText(getResources().getString(R.string.connected));
                autoSize(textView);
                connect.setImageResource(R.drawable.ring_green);
                image = R.drawable.ring_green;
                Intent mIntent = new Intent(ConnectActivity.this, RunActivity.class);
                startActivity(mIntent);

            } else if ("Disconnected".equals(message)) {
                soundPool.play(disconnectedSound,1,1,0,0,1);
                connect.setImageResource(R.drawable.ring_red);
                image = R.drawable.ring_red;
                textView.setText(getResources().getString(R.string.disconnected));
                autoSize(textView);
            } else {
                soundPool.play(failureSound,1,1,0,0,1);
                connect.setImageResource(R.drawable.ring_orange);
                image = R.drawable.ring_orange;
                textView.setText(getResources().getString(R.string.connection_failed));
                autoSize(textView);
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                btService.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                btService.setupService();
            } else {
                msg("Bluetooth was not enabled.");
                finish();
            }
        }
    }

    private ServiceConnection bluetoothConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.MyLocaleBinder binder = (BluetoothService.MyLocaleBinder) service;
            btService = binder.getService();
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private void msg(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    private void autoSize(TextView textView){
        TextViewCompat.setAutoSizeTextTypeWithDefaults(textView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
    }





}
