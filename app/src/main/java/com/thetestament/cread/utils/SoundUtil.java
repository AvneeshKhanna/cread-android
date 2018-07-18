package com.thetestament.cread.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.SettingsFragment;

/**
 * Utility class to provide sound related methods.
 */

public class SoundUtil {

    /**
     * Method to play hatsOff sound.
     *
     * @param context Context to use.
     */
    public static void playHatsOffSound(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean keyHatsOffSound = sp
                .getBoolean(SettingsFragment.KEY_SETTINGS_HATSOFFSOUND, true);

        //if true
        if (keyHatsOffSound) {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.hatsoff);
            //Listener for track completion
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });
            //Play sound
            mediaPlayer.start();
        }
    }
}
