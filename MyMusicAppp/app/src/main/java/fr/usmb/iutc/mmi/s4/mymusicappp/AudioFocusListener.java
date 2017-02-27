package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by Stephane on 27/02/2017.
 */

public class AudioFocusListener implements AudioManager.OnAudioFocusChangeListener {
    private MainActivity app;
    private int audioState = AudioManager.AUDIOFOCUS_LOSS;
    private AudioManager am;
    public AudioFocusListener (MainActivity a){
        app = a;
        am = (AudioManager)app.getSystemService(Context.AUDIO_SERVICE);
    }
    @Override
    public void onAudioFocusChange(int i) {
        audioState = i;
        System.out.println("AudioFocus changed: "+i);
        switch (i) {
            case AudioManager.AUDIOFOCUS_LOSS :
                app.pauseAll();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT :
                app.pauseAll();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK :
                app.pauseAll();
                break;
            case AudioManager.AUDIOFOCUS_GAIN :
                app.restart();
                break;
        }
    }

    public boolean hasFocus(){
        System.out.println("hasFocus: " + audioState);
        return (audioState == AudioManager.AUDIOFOCUS_GAIN);
    }

    public boolean hasOrRequestAudioFocus(){
        System.out.println("hasOrRequestAudioFocus " + this.hasFocus());
        return (this.hasFocus() || this.requestFocus());
    }

    public boolean requestFocus(){
        int res;
        res =  am.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            audioState = AudioManager.AUDIOFOCUS_GAIN;
            app.restart();
        }
        return this.hasFocus();
    }

}
