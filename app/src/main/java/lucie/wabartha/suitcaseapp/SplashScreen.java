package lucie.wabartha.suitcaseapp;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    private SoundPool soundPool;
    private int introSound;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        ImageView trinatLogo = findViewById(R.id.trinatLogo);
        ImageView caseBotLogo = findViewById(R.id.casebotLogo);
        TextView textView = findViewById(R.id.textView);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        introSound = soundPool.load(this, R.raw.intro,1);


        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.mytransition);
        caseBotLogo.startAnimation(myanim);
        trinatLogo.startAnimation(myanim);
        textView.startAnimation(myanim);

        final Intent intent = new Intent(this, ConnectActivity.class);

        myanim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                soundPool.play(introSound,1,1,0,0,1);
            }

            public void onAnimationEnd(Animation animation) {
                startActivity(intent);
            }

            public void onAnimationRepeat(Animation animation) {

            }
        });
/*
        Thread timer = new Thread(){
            public void run() {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    startActivity(intent);
                    finish();
                }
            }
        };
        timer.start();
        */
    }

    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }
}
