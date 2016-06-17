package ee.promobox.promoboxandroid.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import java.lang.ref.WeakReference;

import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;


public class PlayerUIVisibilityRunnable implements Runnable {
    private WeakReference<View> playerControlsLayoutLayoutReference;

    public PlayerUIVisibilityRunnable(View playerControlsLayout){
        playerControlsLayoutLayoutReference = new WeakReference<>(playerControlsLayout);
    }

    public void clear() {
        if (playerControlsLayoutLayoutReference != null) {
            playerControlsLayoutLayoutReference.clear();
            playerControlsLayoutLayoutReference = null;
        }
    }

    @Override
    public void run() {
        View playerControlsLayout = playerControlsLayoutLayoutReference.get();
        if (playerControlsLayout != null) {
            playerControlsLayout.setVisibility(View.INVISIBLE);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(playerControlsLayout.getContext());
            preferences.edit().putInt(FragmentWithSeekBar.PLAYER_UI_VISIBILITY,View.INVISIBLE).apply();
        }
    }
}

