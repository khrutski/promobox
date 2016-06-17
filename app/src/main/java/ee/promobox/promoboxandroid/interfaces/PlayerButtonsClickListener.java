package ee.promobox.promoboxandroid.interfaces;

import android.view.View;

public interface PlayerButtonsClickListener extends View.OnClickListener {
    void  onPlayerPause();
    void  onPlayerPlay();
    void  onPlayerPrevious();
    void  onPlayerNext();
    void  onSettingsPressed();
}
