package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private MediaPlayer[]  mps = new MediaPlayer[10];
    private List<MediaPlayer> onPause= new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bRequestFocus = (Button) this.findViewById(R.id.buttonRequestFocus);
        Button bAbandonFocus = (Button) this.findViewById(R.id.buttonAabandonFocus);
        Button bPause = (Button) this.findViewById(R.id.buttonPause);
        Button bRestart = (Button) this.findViewById(R.id.buttonRestart);
        Button bStop = (Button) this.findViewById(R.id.buttonStop);
        Button b1 = (Button) this.findViewById(R.id.button1);
        b1.setOnClickListener(new ButtonListener(this, 1));
        Button b2 = (Button) this.findViewById(R.id.button2);
        b2.setOnClickListener(new ButtonListener(this, 2));
        Button b3 = (Button) this.findViewById(R.id.button3);
        b3.setOnClickListener(new ButtonListener(this, 3));
        Button b4 = (Button) this.findViewById(R.id.button4);
        b4.setOnClickListener(new ButtonListener(this, 4));
        Button b5 = (Button) this.findViewById(R.id.button5);
        b5.setOnClickListener(new ButtonListener(this, 5));
        Button b6 = (Button) this.findViewById(R.id.button6);
        b6.setOnClickListener(new ButtonListener(this, 6));
        bPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseAll();
            }
        });
        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAll();
            }
        });
        bRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restart();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) this.setSon(requestCode, uri);
        }
    }

    public void setSon(int id, Uri uri){
        System.out.println("URI : "+uri.toString());
        Ringtone son = RingtoneManager.getRingtone(this, uri);
        MediaPlayer mp = MediaPlayer.create(this, uri);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setLooping(false);
        mps[id-1] = mp;
        if (son != null){
             switch (id){
                 case 1 : ((Button) this.findViewById(R.id.button1)).setText(son.getTitle(this)); break;
                 case 2 : ((Button) this.findViewById(R.id.button2)).setText(son.getTitle(this)); break;
                 case 3 : ((Button) this.findViewById(R.id.button3)).setText(son.getTitle(this)); break;
                 case 4 : ((Button) this.findViewById(R.id.button4)).setText(son.getTitle(this)); break;
                 case 5 : ((Button) this.findViewById(R.id.button5)).setText(son.getTitle(this)); break;
                 case 6 : ((Button) this.findViewById(R.id.button6)).setText(son.getTitle(this)); break;
            }
        }
    }
    public MediaPlayer getSon(int id){
        return mps[id-1];
    }
    public void playOrStop(int i){
        if (mps[ i-1] != null) {
            if ( ! mps[i-1].isPlaying()){
                {
                    System.out.println("play "+i);
                    mps[i-1].start();
                }
            } else {
                System.out.println("pause "+i);
                mps[i-1].pause();
            }
        }
    }
    public void stopAll(){
        System.out.println("stop all");
        onPause.clear();
        for (MediaPlayer son : mps ){
            if (son != null){
                son.pause();
                son.seekTo(0);
            }
        }
    }
    public void pauseAll(){
        System.out.println("pause all");
        for (MediaPlayer son : mps ){
            if (son != null && son.isPlaying()){
                son.pause();
                onPause.add(son);
            }
        }
    }
    public void duckAll(){
        System.out.println("duck all");
        for (MediaPlayer son : mps ){
            if (son != null ){
                son.setVolume(0.2f, 0.2f);
            }
        }
    }
    public void unduckAll(){
        System.out.println("duck all");
        for (MediaPlayer son : mps ){
            if (son != null ){
                son.setVolume(1f, 1f);
            }
        }
    }
    public void restart(){
        {
            System.out.println("restart all");
            for (MediaPlayer son : onPause) {
                son.start();
            }
            onPause.clear();
        }
    }

    public void cleanAllMps(){
        for (int i=0; i< mps.length; i++){
            if (mps[i] != null){
                mps[i].reset();
                mps[i].release();
                mps[i] = null;
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() { super.onResume(); }

    @Override
    protected void onPause() { super.onPause(); }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        this.cleanAllMps();
        super.onDestroy();
    }
}
