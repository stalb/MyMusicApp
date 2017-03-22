package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Uri[]  uris = new Uri[10];
    private MyAudioService audioservice;
    private ServiceConnection serviceListener = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyAudioService.MyAudioBinder binder = (MyAudioService.MyAudioBinder)iBinder;
            audioservice = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            audioservice = null;
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
                audioservice.abandonAudioFocus();
            }
        });
        bRequestFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioservice.requestAudioFocus();
            }
        });

        // recupertaion de la ressource musicale (resource raw) et
        // creation l'uri qui correspond a lui :
        // android.resource://fr.usmb.iutc.mmi.s4.mymusicappp/raw/cornichons_mp3"
        Uri.Builder uriBuilder = new Uri.Builder();
        Uri uri1 = uriBuilder.scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(getPackageName()).path("raw/cornichons_mp3").build();
        System.out.println("Uri1 : " + uri1);
        // association avec le boution 1
        uris[0] = uri1;
        //b1.setText("Les cornichons");
        this.setButtonTitle(1, uri1);

        // recuperation d'un fichier audio externe
        // remarque il faut penser a ajouter la permission READ_EXTERNAL_STORAGE dans  AndroidManifest.xml
        File son2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "mp3/Adele/25/01 - Hello.mp3");
        System.out.println("Son2 : " + son2);
        // ontexte si le fichier existe sinon on risque d'avir un pb
        if (son2.exists() && son2.canRead()){
            // transformation en URI et creation du mediaplayer, puis association avec le bouton 2
            Uri uri2 = Uri.fromFile(son2);
            System.out.println("Uri2 : " + uri2);
            uris[1] = uri2;
            //b3.setText("Hello / Adele");
            this.setButtonTitle(2, uri2);
        }

        // recuperation d'un flux sonnore sur internet
        // et association avec le bouton 3
        // RQ : il est necessaire d'ajouter la permission INTERNET dans AndroidManifest.xml
        Uri uri3 = Uri.parse("http://audionautix.com/Music/TexasTechno.mp3");
        uris[2] = uri3;
        //b3.setText("Texas Techno / audionautix.com");
        this.setButtonTitle(3, "Texas Techno / audionautix.com");
        // this.setButtonTitle(3, uri3);
    }

    public void pauseAll() {
        audioservice.pauseAll();
    }

    public void restart() {
        audioservice.restart();
    }

    public void stopAll() {
        Intent serviceIntent = new Intent(MainActivity.this, MyAudioService.class);
        stopService(serviceIntent);
        audioservice.stopAll();
    }

    public void addToPlaylistAsync(Uri uri) {
        audioservice.addToPlaylistAsync(uri);
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

    public void setButtonTitle(final int id,final String title) {
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

    public void setButtonTitle(final int id,final Uri uri){
        System.out.println("URI : "+uri.toString());
        MediaMetadataRetriever dataManager = new MediaMetadataRetriever();
        String title = "inconnu";
        if ("file".equalsIgnoreCase(uri.getScheme())
                || "content".equalsIgnoreCase(uri.getScheme())
                || ContentResolver.SCHEME_ANDROID_RESOURCE.equalsIgnoreCase(uri.getScheme())) {
            dataManager.setDataSource(this, uri);
            String trackTitle = dataManager.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String trackAuthor = dataManager.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            title = ((trackTitle != null) ? trackTitle : "inconnu" )+ " / " + ((trackAuthor != null) ? trackAuthor : "inconnu" );
        } else {
            title = uri.getLastPathSegment();
        }
        this.setButtonTitle(id, title);
    }

    public Uri getSon(int id){
        return uris[id-1];
    }

    public void addToPlaylist(int i){
        // si l'URI n'est pas null on cree le mediaplayer correspondant
        // puis on l'ajoute dans a la fin de la playlist en arriere plan
        if (uris[i-1] != null) {
            this.addToPlaylistAsync(uris[i-1]);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, MyAudioService.class);
        this.startService(serviceIntent);
        this.bindService(serviceIntent, serviceListener, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() { super.onResume(); }

    @Override
    protected void onPause() { super.onPause(); }

    @Override
    protected void onStop() {
        this.unbindService(serviceListener);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
