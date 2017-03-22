package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

/**
 * Created by Stephane on 27/02/2017.
 */

public class MyAudioBroadcastReceiver extends BroadcastReceiver {
    private MyAudioService service;

    public MyAudioBroadcastReceiver (MyAudioService s){
        service = s;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())){
            service.pauseAll();
        }
    }
}
