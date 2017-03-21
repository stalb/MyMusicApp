package fr.usmb.iutc.mmi.s4.mymusicappp;

import android.content.Intent;
import android.media.RingtoneManager;
import android.view.View;


/**
 * Created by Stephane on 27/02/2017.
 */

public class ButtonListener implements View.OnClickListener {
    private MainActivity mainActivity;
    private int id;
    public ButtonListener (MainActivity a, int id){
        mainActivity = a;
        this.id = id;
    }
    @Override
    public void onClick(View view) {
        this.searchSound(id);
    }
    private void searchSound(int code){
        if (mainActivity.getSon(code) == null) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            mainActivity.startActivityForResult(intent, code);
        } else {
            mainActivity.addToPlaylist(code);
        }
    }
}
