package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
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

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver noisyBroacastReceiver ;
    private MyAudioFocusManager audioFocusManager;
    private LinkedList<MediaPlayer> playlist = new LinkedList<>();
    private Uri[]  uris = new Uri[10];
    private ExecutorService backgroundThread ;

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
    @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            // supression du 1er element de la playlist
            MediaPlayer mp = playlist.pollFirst();
            // liberation des resources associees au mediaplayer
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

        // creation du pool de thread pour les taches en arriere plan (1 seul thread)
        backgroundThread = Executors.newSingleThreadExecutor();

        // recupertaion de la ressource musicale (resource raw) et
        // creation l'uri qui correspond a lui :
        // android.resource://fr.usmb.iutc.mmi.s4.mymusicappp/raw/cornichons_mp3"
        Uri.Builder uriBuilder = new Uri.Builder();
        Uri uri1 = uriBuilder.scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(getPackageName()).path("raw/cornichons_mp3").build();
        System.out.println("Uri1 : " + uri1);
        // association avec le boution 1
        uris[0] = uri1;
        //b1.setText("Les cornichons");
        this.setButtonTitle(1,uri1);

        // recuperation d'un fichier audio externe
        // remarque il faut penser a ajouter la permission READ_EXTERNAL_STORAGE dans  AndroidManifest.xml
        File son2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "mp3/Adele/25/01 - Hello.mp3");
        System.out.println("Son2 : " + son2);
        // transformation en URI et creation du mediaplayer, puis association avec le bouton 2
        Uri uri2 = Uri.fromFile(son2);
        System.out.println("Uri2 : " + uri2);
        uris[1] = uri2;
        this.setButtonTitle(2, uri2);

        // recuperation d'un flux sonnore sur internet
        // et association avec le bouton 3
        // RQ : il est necessaire d'ajouter la permission INTERNET dans AndroidManifest.xml
        Uri uri3 = Uri.parse("http://audionautix.com/Music/TexasTechno.mp3");
        uris[2] = uri3;
        this.setButtonTitle(3, uri3);
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
        uris[id-1] = uri;
    }

    public void setButtonTitle(int id, Uri uri){
        System.out.println("URI : "+uri.toString());
        MediaMetadataRetriever dataManager = new MediaMetadataRetriever();
        if ("file".equalsIgnoreCase(uri.getScheme())
                || "content".equalsIgnoreCase(uri.getScheme())
                || ContentResolver.SCHEME_ANDROID_RESOURCE.equalsIgnoreCase(uri.getScheme())) {
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

    public Uri getSon(int id){
        return uris[id-1];
    }

    public void addToPlaylist(int i){
        // si l'URI n'est pas null on cree le mediaplayer correspondant
        // puis on l'ajoute dans a la fin de la playlist
        if (uris[i-1] != null) {
            MediaPlayer nouveau = MediaPlayer.create(this, uris[i-1]);
            // association au MediaPlyer.OnCompletionListener pour savoir quand le morceau est termine
            nouveau.setOnCompletionListener(onCompletionListener);
            // si on est en mode bas niveau sonore, on l'applique au nouveau mediaplayer
            if (audioFocusManager.canDuck()) {
                nouveau.setVolume(0.2f, 0.2f);
                }
            MediaPlayer dernier = playlist.peekLast();
            if (dernier != null) {
                dernier.setNextMediaPlayer(nouveau);
            }
            playlist.addLast(nouveau);
            }
        // si le 1er element de la playlist n'est pas en cours de lceture
        // on essaye de le lancer (quand c'est possible)
        MediaPlayer mp = playlist.getFirst();
        if (! mp.isPlaying() && (audioFocusManager.canDuck() || audioFocusManager.hasOrRequestAudioFocus())){
            System.out.println("starting 1st element");
            mp.start();
        }
    }

    public void stopAll(){
        System.out.println("stop playlist");
        for (MediaPlayer son : playlist ){
            if (son != null){
                son.pause();
                son.seekTo(0);
                // on libere les resources associes au mediaplayer
                son.release();
            }
        }
        // on vide la playlist
        playlist.clear();

    }
    public void pauseAll(){
        System.out.println("pause playlist");
        for (MediaPlayer son : playlist ){
            if (son != null && son.isPlaying()){
                son.pause();
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
        System.out.println("unduck all");
        for (MediaPlayer son : playlist ){
            if (son != null ){
                son.setVolume(1f, 1f);
            }
        }
    }
    public void restart(){
        // avant de relancer la musique on verifie
        // si on a le focus audio et eventuellement on le demande
        if (audioFocusManager.canDuck() || audioFocusManager.hasOrRequestAudioFocus()) {
            System.out.println("restart playlist");
            MediaPlayer son = playlist.peekFirst();
            if (son != null) {
                son.start();
            }
        } else {
            System.out.println("interdit : pas de focus audio");
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
        // arret du pool de thread
        backgroundThread.shutdownNow();
        try {
            // on attend l'arret effectif du poll de thread
            backgroundThread.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // arret de la playlist
        this.stopAll();
        // de-enregistrement du broadcastReceiver
        this.unregisterReceiver(noisyBroacastReceiver);

        // avant de quiter on abandonne le focus audio
        audioFocusManager.abandonAudioFocus();

        super.onDestroy();
    }
}
