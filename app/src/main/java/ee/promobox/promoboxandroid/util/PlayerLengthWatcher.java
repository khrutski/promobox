package ee.promobox.promoboxandroid.util;

import android.util.Log;

import java.lang.ref.WeakReference;

import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;

public class PlayerLengthWatcher  implements Runnable {
    private WeakReference<FragmentWithSeekBar> fragmentReference;
    private WeakReference<FragmentPlaybackListener> playbackListenerReference;

    public PlayerLengthWatcher(FragmentWithSeekBar fragment, FragmentPlaybackListener playbackListener){
        fragmentReference = new WeakReference<>(fragment);
        playbackListenerReference = new WeakReference<>(playbackListener);
    }

    public void clear() {
        fragmentReference.clear();
        playbackListenerReference.clear();
        fragmentReference = null;
        playbackListenerReference = null;
    }

    @Override
    public void run() {
        Log.e("PlayerLengthWatcher", "Executing runnable, smth wrong with player");
        FragmentWithSeekBar fragment = fragmentReference.get();
        FragmentPlaybackListener playbackListener = playbackListenerReference.get();
        if (fragment != null && playbackListener != null){
            fragment.cleanUp();
            playbackListener.onPlayBackRunnableError();
            playbackListener.onPlaybackStop();
        }
    }
}