package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Uri[]  uris = new Uri[10];
    private ExecutorService backgroundThread ;
    private MyAudioService audioService;
    private Intent serviceIntent ;

    // Interface de connexion au service
    private ServiceConnection myConnectionListener = new ServiceConnection() {
        // Se déclenche quand l'activité se connecte au service
        public void onServiceConnected(ComponentName className, IBinder binder) {
            audioService = ((MyAudioService.MyBinder)binder).getAudioService();
        }
        // Se déclenche dès que le service est déconnecté
        public void onServiceDisconnected(ComponentName className) {
            audioService = null;
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

        // activation des boutons de gestion manuelle du focus audio
        bAbandonFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abandonAudioFocus();
            }
        });
        bRequestFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAudioFocus();
            }
        });

        // creation de l'intent du service
        serviceIntent = new Intent(this, MyAudioService.class);
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
        this.setButtonTitleAsync(1,uri1);

        // recuperation d'un fichier audio externe
        // remarque il faut penser a ajouter la permission READ_EXTERNAL_STORAGE dans  AndroidManifest.xml
        File son2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "mp3/Adele/25/01 - Hello.mp3");
        System.out.println("Son2 : " + son2);
        if (son2.exists()) {
            // transformation en URI et creation du mediaplayer, puis association avec le bouton 2
            Uri uri2 = Uri.fromFile(son2);
            System.out.println("Uri2 : " + uri2);
            uris[1] = uri2;
            b2.setText("Hello");
            this.setButtonTitleAsync(2, uri2);
        } else {
            System.out.println("erreur: "+son2 + " non existant !!!");
        }

        // recuperation d'un flux sonnore sur internet
        // et association avec le bouton 3
        // RQ : il est necessaire d'ajouter la permission INTERNET dans AndroidManifest.xml
        Uri uri3 = Uri.parse("http://audionautix.com/Music/TexasTechno.mp3");
        uris[2] = uri3;
        this.setButtonTitleAsync(3, uri3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                this.setSon(requestCode, uri);
                this.setButtonTitleAsync(requestCode, uri);
            }
        }
    }

    public void setSon(int id, Uri uri){
        System.out.println("URI : "+uri.toString());
        uris[id-1] = uri;
    }

    public void setButtonTitle(final int id,final Uri uri){
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
        final String title = ((trackTitle != null) ? trackTitle : "inconnu" )+ " / " + ((trackAuthor != null) ? trackAuthor : "inconnu" );
        // la mise a jour du titre des bouton doit se faire dans le thread d'interface utilisateur
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (title != null){
                    switch (id){
                        case 1 : ((Button) MainActivity.this.findViewById(R.id.button1)).setText(title); break;
                        case 2 : ((Button) MainActivity.this.findViewById(R.id.button2)).setText(title); break;
                        case 3 : ((Button) MainActivity.this.findViewById(R.id.button3)).setText(title); break;
                        case 4 : ((Button) MainActivity.this.findViewById(R.id.button4)).setText(title); break;
                        case 5 : ((Button) MainActivity.this.findViewById(R.id.button5)).setText(title); break;
                        case 6 : ((Button) MainActivity.this.findViewById(R.id.button6)).setText(title); break;
                    }
                }
            }
        });
    }

    // mise a jour du titre du bouton via le pool de thread
    public void setButtonTitleAsync(final int i, final Uri uri){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                setButtonTitle(i, uri);
            }
        };
        backgroundThread.execute(task);
    }

    public Uri getSon(int id){
        return uris[id-1];
    }

    public void addToPlaylistAsync(int i){
        // on verifie que le service est bien lance
        this.startService(serviceIntent);
        // si l'URI n'est pas null on l'ajoute dans a la fin de la playlist
        if (uris[i-1] != null) {
            this.addToPlaylistAsync(uris[i-1]);
        }
     }

    public void pauseAll() {
        audioService.pauseAll();
    }

    public void restart() {
        audioService.restart();
    }

    public void stopAll() {
        audioService.stopAll();
    }

    public void abandonAudioFocus() {
        audioService.abandonAudioFocus();
    }

    public boolean requestAudioFocus() {
        return audioService.requestAudioFocus();
    }

    public void addToPlaylistAsync(Uri uri) {
        audioService.addToPlaylistAsync(uri);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(serviceIntent, myConnectionListener, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() { super.onResume(); }

    @Override
    protected void onPause() { super.onPause(); }

    @Override
    protected void onStop() {
        if (audioService != null){
            unbindService(myConnectionListener);
        }
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

        super.onDestroy();
    }
}
