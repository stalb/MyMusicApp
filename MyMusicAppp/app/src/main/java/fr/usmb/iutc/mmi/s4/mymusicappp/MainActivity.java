package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private MediaPlayer[]  mps = new MediaPlayer[10];
    private Uri[]  uris = new Uri[10];
    private IntentFilter noisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BroadcastReceiver audioBroacastReceiver ;
    private List<MediaPlayer> onPause= new LinkedList<>();
    private AudioFocusListener afl;
    private LinkedList<MediaPlayer > playlist = new LinkedList<>();
    private ExecutorService backgroundThread ;

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            MediaPlayer mp = playlist.pollFirst();
            mp.release();
        }
    };

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
        audioBroacastReceiver = new MyAudioBroadcastReceiver(this);
        this.registerReceiver(audioBroacastReceiver, noisyFilter);
        afl = new AudioFocusListener(this);
        bAbandonFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                afl.abandonAudioFocus();
            }
        });
        bRequestFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                afl.requestAudioFocus();
            }
        });
        File son1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "mp3/Adele/25/01 - Hello.mp3");
        System.out.println("Son1 : " + son1);
        Uri uri1 = Uri.fromFile(son1);
        System.out.println("Uri1 : " + uri1);
        MediaPlayer mp1 = MediaPlayer.create(this, uri1);
        mps[0] = mp1;
        uris[0] = uri1;
        b1.setText("Hello");

        Uri uri2 = Uri.parse("http://audionautix.com/Music/TexasTechno.mp3");
        MediaPlayer mp2 = MediaPlayer.create(this, uri2);
        mps[1] = mp2;
        uris[1] = uri2;
        b2.setText("TexasTechno");


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
        mp.setLooping(false);
        mps[id-1] = mp;
        uris[id-1] = uri;
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
    public MediaPlayer
    getSon(int id){
        return mps[id-1];
    }
    public void playOrStop(int i){
        if (mps[ i-1] != null) {
            if ( ! mps[i-1].isPlaying()){
                if (afl.hasOrRequestAudioFocus() ) {
                    System.out.println("play "+i);
                    mps[i-1].start();
                }
            } else {
                System.out.println("pause "+i);
                mps[i-1].pause();
            }
        }
    }

    public void addToPlaylist(int i){
        if (uris[ i-1] != null) {
            MediaPlayer nouveau = MediaPlayer.create(this, uris[ i-1]);
            nouveau.setOnCompletionListener(onCompletionListener);
            MediaPlayer dernier = playlist.peekLast();
            if (dernier != null) {
                dernier.setNextMediaPlayer(nouveau);
            }
            playlist.addLast(nouveau);
        }
        MediaPlayer mp = playlist.getFirst();
        if (! mp.isPlaying() && afl.hasOrRequestAudioFocus()){
            mp.start();
        }
    }

    public void addToPlaylistAsync(final int i){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                addToPlaylist(i);
            }
        };
        backgroundThread.execute(task);
    }

    public void stopAll(){
        System.out.println("stop all");
        onPause.clear();
        for (MediaPlayer son : playlist ){
            if (son != null){
                son.pause();
                son.seekTo(0);
                son.release();
            }
        }
        playlist.clear();
    }
    public void pauseAll(){
        System.out.println("pause all");
        for (MediaPlayer son : playlist ){
            if (son != null && son.isPlaying()){
                son.pause();
                //onPause.add(son);
            }
        }
    }
    public void duckAll(){
        System.out.println("duck all");
        for (MediaPlayer son : playlist ){
            if (son != null ){
                son.setVolume(0.2f, 0.2f);
            }
        }
    }
    public void unduckAll(){
        System.out.println("duck all");
        for (MediaPlayer son : playlist ){
            if (son != null ){
                son.setVolume(1f, 1f);
            }
        }
    }
    public void restart(){
        if (afl.hasAudioFocus()) {
            System.out.println("restart all");
            MediaPlayer son = playlist.peekFirst();
            if (son != null) {
                son.start();
            }
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        this.stopAll();
        this.cleanAllMps();
        afl.abandonAudioFocus();
        this.unregisterReceiver(audioBroacastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        backgroundThread.shutdownNow();
        try {
            backgroundThread.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.pauseAll();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
         backgroundThread = Executors.newSingleThreadExecutor();
        this.restart();
    }
}
