package ee.promobox.promoboxandroid.util;

import android.widget.SeekBar;

import java.lang.ref.WeakReference;


public class SeekBarProgressChangerRunnable implements Runnable {

    private boolean isKilled = false;

    private final WeakReference<SeekBar> seekBarWeakReference;

    public SeekBarProgressChangerRunnable(SeekBar seekBar){
        seekBarWeakReference = new WeakReference<>(seekBar);
    }

    @Override
    public void run() {
        SeekBar seekBar = seekBarWeakReference.get();
        if (!isKilled && seekBar != null && seekBar.getProgress() < seekBar.getMax()) {
            seekBar.incrementProgressBy(100);
            seekBar.postDelayed(this, 100);
        }
    }
}
