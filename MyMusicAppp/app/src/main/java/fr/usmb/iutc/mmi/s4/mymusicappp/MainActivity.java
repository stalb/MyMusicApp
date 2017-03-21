package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private MediaPlayer[]  mps = new MediaPlayer[10];
    private List<MediaPlayer> onPause= new LinkedList<>();
    private BroadcastReceiver noisyBroacastReceiver ;
    private MyAudioFocusManager audioFocusManager;

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

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // creation en enregistrement du BroadcastReceiver
        noisyBroacastReceiver = new MyAudioBroadcastReceiver(this);
        IntentFilter noisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(noisyBroacastReceiver, noisyFilter);

        // creation et enregistrement du gestionaire de focus audio
        audioFocusManager = new MyAudioFocusManager(this);

        // activation des boutons de gestion manuelle du focus audio
        bAbandonFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioFocusManager.abandonAudioFocus();
            }
        });
        bRequestFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioFocusManager.requestAudioFocus();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                this.setSon(requestCode, uri);
                this.setButtonTitle(requestCode, uri);
            }
        }
    }

    public void setSon(int id, Uri uri){
        System.out.println("URI : "+uri.toString());
        MediaPlayer mp = MediaPlayer.create(this, uri);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // si on est en mode bas niveau sonore, on l'applique au nouveau son
        if (audioFocusManager.canDuck()) {
            mp.setVolume(0.2f, 0.2f);
        }
        mps[id-1] = mp;
    }

    public void setButtonTitle(int id, Uri uri){
        System.out.println("URI : "+uri.toString());
        MediaMetadataRetriever dataManager = new MediaMetadataRetriever();
        if ("file".equalsIgnoreCase(uri.getScheme()) || "content".equalsIgnoreCase(uri.getScheme())){
            dataManager.setDataSource(this, uri);
        } else {
            dataManager.setDataSource(uri.toString(), new HashMap<String, String>());
        }
        String trackTitle = dataManager.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String trackAuthor = dataManager.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String title= ((trackTitle != null) ? trackTitle : "inconnu" )+ " / " + ((trackAuthor != null) ? trackAuthor : "inconnu" );
        if (title != null){
            switch (id){
                case 1 : ((Button) this.findViewById(R.id.button1)).setText(title); break;
                case 2 : ((Button) this.findViewById(R.id.button2)).setText(title); break;
                case 3 : ((Button) this.findViewById(R.id.button3)).setText(title); break;
                case 4 : ((Button) this.findViewById(R.id.button4)).setText(title); break;
                case 5 : ((Button) this.findViewById(R.id.button5)).setText(title); break;
                case 6 : ((Button) this.findViewById(R.id.button6)).setText(title); break;
            }
        }
    }

    public MediaPlayer getSon(int id){
        return mps[id-1];
    }
    public void playOrStop(int i){
        if (mps[ i-1] != null) {
            if ( ! mps[i-1].isPlaying()){
                // avant de demarrer le son on verifie si c'est possible
                // ou on demande le focus audio
                if (audioFocusManager.canDuck() || audioFocusManager.hasOrRequestAudioFocus()){
                    System.out.println("play "+i);
                    mps[i-1].start();
                } else {
                    System.out.println("interdit : pas de focus audio");
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
        // avant de relancer la musique on verifie
        // si on a le focus audio et eventuellement on le demande
        if (audioFocusManager.canDuck() || audioFocusManager.hasOrRequestAudioFocus()) {
            System.out.println("restart all");
            for (MediaPlayer son : onPause) {
                son.start();
            }
            onPause.clear();
        } else {
            System.out.println("interdit : pas de focus audio");
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
        // de-enregistrement du broadcastReceiver
        this.unregisterReceiver(noisyBroacastReceiver);

        // avant de quiter on abandonne le focus audio
        audioFocusManager.abandonAudioFocus();

        super.onDestroy();
    }
}
