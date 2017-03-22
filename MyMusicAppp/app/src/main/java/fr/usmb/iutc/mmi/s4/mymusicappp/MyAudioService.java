package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyAudioService extends Service {

    private BroadcastReceiver noisyBroacastReceiver ;
    private MyAudioFocusManager audioFocusManager;
    private LinkedList<MediaPlayer> playlist = new LinkedList<>();

    private ExecutorService backgroundThread ;
    private Intent activityIntent;
    private PendingIntent notificationIntent;
    private Notification notification;

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            // supression du 1er element de la playlist
            MediaPlayer mp = playlist.pollFirst();
            // liberation des resources associees au mediaplayer
            mp.release();
            if (playlist.isEmpty()){
                stopForeground(true);
                stopSelf();
            }
        }
    };

    public MyAudioService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // creation en enregistrement du BroadcastReceiver
        noisyBroacastReceiver = new MyAudioBroadcastReceiver(this);
        IntentFilter noisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(noisyBroacastReceiver, noisyFilter);

        // creation et enregistrement du gestionaire de focus audio
        audioFocusManager = new MyAudioFocusManager(this);

        // creation du pool de thread pour les taches en arriere plan (1 seul thread)
        backgroundThread = Executors.newSingleThreadExecutor();

        activityIntent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent = PendingIntent.getActivity(this, 123, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new Notification.Builder(this)
                .setContentTitle("AudioService")
                .setContentText(" est actif")
                .setContentIntent(notificationIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

    }


    public void addToPlaylist(Uri uri){
        // si l'URI n'est pas null on cree le mediaplayer correspondant
        // puis on l'ajoute dans a la fin de la playlist
        if (uri != null) {
            MediaPlayer nouveau = MediaPlayer.create(this, uri);
            // si la resource n'est pas eccessible, nouveau peut etre nul !
            if (nouveau != null ) {
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
                this.startForeground(456,notification);
            } else {
                System.out.println("Resource: " + uri + " non accessible !!");
            }
        }
        // si le 1er element de la playlist n'est pas en cours de lecture
        // on essaye de le lancer (quand c'est possible)
        MediaPlayer mp = playlist.peekFirst();
        if (mp != null && ! mp.isPlaying() && (audioFocusManager.canDuck() || audioFocusManager.hasAudioFocus())){
            System.out.println("starting 1st element");
            mp.start();
        }
    }

    // ajout dans la playlist en utilisant le pool de thread
    public void addToPlaylistAsync(final Uri uri){
        // avant d'ajouter le morceau on demande si necessaire le focus audio
        if (! (audioFocusManager.canDuck() || audioFocusManager.hasAudioFocus())) audioFocusManager.requestAudioFocus();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                addToPlaylist(uri);
            }
        };
        backgroundThread.execute(task);
    }

    public void stopAll(){
        System.out.println("stop playlist");
        //on met en pause
        this.pauseAll();
        // et on abandone le focus audio
        audioFocusManager.abandonAudioFocus();
        // on libere les elements de la playlist
        for (MediaPlayer son : playlist ){
            if (son != null){
                // on libere les resources associes au mediaplayer
                son.release();
            }
        }
        // on vide la playlist
        playlist.clear();
        stopForeground(true);

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

    public void abandonAudioFocus() {
        audioFocusManager.abandonAudioFocus();
    }

    public boolean requestAudioFocus() {
        return audioFocusManager.requestAudioFocus();
    }

    @Override
    public void onDestroy() {
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

        Log.v("MyServiceAudio", "arret du service audio");
        super.onDestroy();
    }

    public class MyAudioBinder extends Binder {
        MyAudioService getService(){
            return MyAudioService.this;
        }
    }
    private final MyAudioBinder binder = new MyAudioBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
     }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
