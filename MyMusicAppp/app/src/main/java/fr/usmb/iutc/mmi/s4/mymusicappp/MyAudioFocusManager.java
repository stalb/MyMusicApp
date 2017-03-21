package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.Context;
import android.media.AudioManager;

public class MyAudioFocusManager  implements AudioManager.OnAudioFocusChangeListener{

    private MainActivity myActivity;
    private int audioState = AudioManager.AUDIOFOCUS_LOSS;
    private AudioManager audioManager ;

    public MyAudioFocusManager(MainActivity myActivity) {
        this.myActivity = myActivity;
        audioManager = (AudioManager)myActivity.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onAudioFocusChange(int i) {
        audioState = i;
        switch (i) {
            case AudioManager.AUDIOFOCUS_LOSS :
                myActivity.pauseAll();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT :
                myActivity.pauseAll();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK :
                myActivity.duckAll();
                break;
            case AudioManager.AUDIOFOCUS_GAIN :
                myActivity.unduckAll();
                myActivity.restart();
                break;

        }
    }
    public boolean requestFocusAudio(){
       int res = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            audioState = AudioManager.AUDIOFOCUS_GAIN;
        } else {
            audioState = AudioManager.AUDIOFOCUS_LOSS;
        }
        return (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }
    public boolean hasFocusAudio(){
        return (audioState == AudioManager.AUDIOFOCUS_GAIN);
    }
    public boolean hasOrRequestFocusAudio () {
        return (this.hasFocusAudio() || this.requestFocusAudio());
    }
    public void abandonFocusAudio(){
        audioState = AudioManager.AUDIOFOCUS_LOSS;
        myActivity.pauseAll();
        audioManager.abandonAudioFocus(this);
    }
}
