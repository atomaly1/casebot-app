package lucie.wabartha.suitcaseapp.ui.run;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import lucie.wabartha.suitcaseapp.BluetoothService;
import lucie.wabartha.suitcaseapp.R;

public class PlaceholderFragment extends Fragment implements View.OnClickListener{

    private static final String ARG_SECTION_NUMBER = "section_number";

    BluetoothService btService;
    Boolean isBound = false;

    TextView mTextView;
    ImageButton mImageButton;

    ProgressBar mVelocityBar;
    ProgressBar mBatteryBar;
    TextView mVelocity, mBattery;

    private SoundPool soundPool;
    private int buttonSound;

    final static String ON = "1";
    final static String OFF = "2";
    final static String filterValue = "BluetoothValueUpdate";
    final static String keyValue = "Value";


    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PageViewModel pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        buttonSound = soundPool.load(getContext(), R.raw.mouth,1);

        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);

        Intent mIntent = new Intent(getContext(), BluetoothService.class);
        if (getActivity() != null){
            getActivity().bindService(mIntent,bluetoothConnection, Context.BIND_AUTO_CREATE);
        }
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = null;

        if (getArguments() != null) {
            switch (getArguments().getInt(ARG_SECTION_NUMBER))
            {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_run, container, false);

                    mTextView = rootView.findViewById(R.id.run_textView);
                    mImageButton = rootView.findViewById(R.id.run_button);

                    mImageButton.setOnClickListener(this);

                    break;

                case 2:
                    //load another page
                    rootView = inflater.inflate(R.layout.fragment_spec, container, false);

                    mVelocityBar = rootView.findViewById(R.id.velocity_bar);
                    mBatteryBar = rootView.findViewById(R.id.battery_bar);

                    mVelocity = rootView.findViewById(R.id.velocity_val);
                    mBattery = rootView.findViewById(R.id.baterry_val);

                    if (getContext() != null){
                        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mValueReceiver,
                                new IntentFilter(filterValue));
                    }

                    //mVelocity.setText("3.2");
                    mBattery.setText("57");

                    mVelocityBar.setProgress(34);
                    mBatteryBar.setProgress(57);

                    break;
            }
        }

        return rootView;
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    public void onDestroy() {
        btService.sendData(OFF);
        btService.stopService();
        super.onDestroy();
    }

    public void onClick(View v) {
        if (mTextView.getText() == getResources().getString(R.string.run)){
            soundPool.play(buttonSound,1,1,0,0,1);
            btService.sendData(ON);
            mTextView.setText(getResources().getString(R.string.stop));
            autoSize(mTextView);
            mImageButton.setImageResource(R.drawable.ring_red);
        } else {
            soundPool.play(buttonSound,1,1,0,0,1);
            btService.sendData(OFF);
            mTextView.setText(getResources().getString(R.string.run));
            autoSize(mTextView);
            mImageButton.setImageResource(R.drawable.ring_green);
        }
    }

    private void autoSize(TextView textView){
        TextViewCompat.setAutoSizeTextTypeWithDefaults(textView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
    }

    private BroadcastReceiver mValueReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(keyValue);
            mVelocity.setText(message);

            try {
                float f = 0;
                if (message != null) {
                    f = Float.parseFloat(message)*20;
                }
                int velocity = Math.round(f);
                mVelocityBar.setProgress(velocity);
            }catch (NumberFormatException n){
                msg("Fail to parse Int");
            }


        }
    };

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
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
    }
}
