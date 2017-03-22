package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.Context;
import android.media.AudioManager;

public class MyAudioFocusManager  implements AudioManager.OnAudioFocusChangeListener{

    private MyAudioService audioService;
    private int audioState = AudioManager.AUDIOFOCUS_LOSS;
    private AudioManager audioManager ;

    public MyAudioFocusManager(MyAudioService audioService) {
        this.audioService = audioService;
        audioManager = (AudioManager)audioService.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onAudioFocusChange(int i) {
        audioState = i;
        System.out.println("AudioFocus changed: "+i);
        switch (i) {
            case AudioManager.AUDIOFOCUS_LOSS :
                audioService.pauseAll();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT :
                audioService.pauseAll();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK :
                audioService.duckAll();
                break;
            case AudioManager.AUDIOFOCUS_GAIN :
                audioService.unduckAll();
                audioService.restart();
                break;
        }
    }

    public boolean canDuck() {
        System.out.println("canduck: " + audioState);
        return (audioState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);
    }
    public boolean hasAudioFocus(){
        System.out.println("hasFocus: " + audioState);
        return (audioState == AudioManager.AUDIOFOCUS_GAIN);
    }

    public boolean hasOrRequestAudioFocus() {
        System.out.println("hasOrRequestAudioFocus " + this.hasAudioFocus());
        return (this.hasAudioFocus() || this.requestAudioFocus());
    }
    public boolean requestAudioFocus(){
        int res = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            audioState = AudioManager.AUDIOFOCUS_GAIN;
            audioService.unduckAll();
            //myActivity.restart();
        } else {
            audioState = AudioManager.AUDIOFOCUS_LOSS;
        }
        return (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    public void abandonAudioFocus(){
        audioState = AudioManager.AUDIOFOCUS_LOSS;
        audioService.pauseAll();
        audioManager.abandonAudioFocus(this);
    }
}
