package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private Ringtone[]  sons = new Ringtone[10];
    private MediaPlayer[]  mps = new MediaPlayer[10];
    //private SoundPool soundPool;
    private IntentFilter noisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BroadcastReceiver audioBroacastReceiver ;
//    private List<Ringtone> onPause= new LinkedList<>();
    private List<MediaPlayer> onPause= new LinkedList<>();
    private AudioFocusListener afl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b1 = (Button) this.findViewById(R.id.button1);
        b1.setOnClickListener(new ButtonListener(this, 1));
        Button b2 = (Button) this.findViewById(R.id.button2);
        b2.setOnClickListener(new ButtonListener(this, 2));
        // SoundPool.Builder  builder = new SoundPool.Builder();
        //soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioBroacastReceiver = new MyAudioBroadcastReceiver(this);
        this.registerReceiver(audioBroacastReceiver, noisyFilter);
        afl = new AudioFocusListener(this);
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
        sons[id-1] = son;
        mps[id-1] = mp;
        if (son != null){
            son.setStreamType(AudioManager.STREAM_MUSIC);
            switch (id){
                case 1 : ((Button) this.findViewById(R.id.button1)).setText(son.getTitle(this)); break;
                case 2 : ((Button) this.findViewById(R.id.button2)).setText(son.getTitle(this)); break;
            }
        }
    }
    public Ringtone getSon(int id){
        return sons[id-1];
    }
    public void playOrStop(int i){
//        if (sons[ i-1] != null) {
//            if ( ! sons[i-1].isPlaying()){
//                if (afl.hasOrRequestAudioFocus() ) {
//                    System.out.println("play "+i);
//                    sons[i-1].play();
//                }
//            } else {
//                sons[i-1].stop();
//            }
//        }
        if (mps[ i-1] != null) {
            if ( ! mps[i-1].isPlaying()){
                if (afl.hasOrRequestAudioFocus() ) {
                    System.out.println("play "+i);
                    mps[i-1].start();
                }
            } else {
                mps[i-1].pause();
            }
        }
    }
    public void stopAll(){
        System.out.println("stop all");
//        for (Ringtone son : sons ){
//            if (son != null){
//                son.stop();
//            }
//        }
        for (MediaPlayer son : mps ){
            if (son != null){
                son.reset();
            }
        }
    }
    public void pauseAll(){
        System.out.println("pause all");
//        for (Ringtone son : sons ){
//            if (son != null){
//                son.stop();
//                onPause.add(son);
//            }
//        }
        for (MediaPlayer son : mps ){
            if (son != null){
                son.pause();
                onPause.add(son);
            }
        }
    }
    public void restart(){
        if (afl.hasOrRequestAudioFocus()) {
            System.out.println("restart all");
//            for (Ringtone son : onPause) {
//                son.play();
//            }
            for (MediaPlayer son : onPause) {
                son.start();
            }
            onPause.clear();
        }
    }
}
