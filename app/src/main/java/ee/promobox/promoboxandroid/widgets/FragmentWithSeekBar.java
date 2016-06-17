package ee.promobox.promoboxandroid.widgets;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.R;
import ee.promobox.promoboxandroid.interfaces.PlayerButtonsClickListener;
import ee.promobox.promoboxandroid.util.PlayerUIVisibilityRunnable;
import ee.promobox.promoboxandroid.util.SeekBarProgressChangerRunnable;
import ee.promobox.promoboxandroid.util.TimeUtil;


public abstract class FragmentWithSeekBar extends Fragment implements PlayerButtonsClickListener ,
        SeekBar.OnSeekBarChangeListener{
    public static final String PLAYER_UI_VISIBILITY = "playerUIVisibility";
    private static final String TAG = "FragmentWithSeekBar";
    private static final long VISIBILITY_DELAY_MS = 10*1000;

    private Handler playerUIVisibilityHandler = new Handler();
    private PlayerUIVisibilityRunnable visibilityRunnable;
    private SeekBarProgressChangerRunnable seekBarProgressChanger;

    private boolean paused = false;

    private SeekBar seekBar;
    private View playerControlsLayout;

    @Override
    public void onDestroyView() {
        seekBar.removeCallbacks(seekBarProgressChanger);
        Button pauseButton = (Button) playerControlsLayout.findViewById(R.id.player_pause);
        Button previousButton = (Button) playerControlsLayout.findViewById(R.id.player_back);
        Button nextButton = (Button) playerControlsLayout.findViewById(R.id.player_next);
        Button settingsButton = (Button) playerControlsLayout.findViewById(R.id.player_settings);
        pauseButton.setOnClickListener(null);
        previousButton.setOnClickListener(null);
        nextButton.setOnClickListener(null);
        settingsButton.setOnClickListener(null);
        playerUIVisibilityHandler.removeCallbacks(visibilityRunnable);
        visibilityRunnable.clear();
        visibilityRunnable = null;

        playerControlsLayout = null;
        seekBar = null;
        seekBarProgressChanger = null;

        super.onDestroyView();
    }

    protected void setView(View view) {
        playerControlsLayout = view;
        seekBar = (SeekBar) playerControlsLayout.findViewById(R.id.audio_seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setProgressDrawable( getResources().getDrawable(R.drawable.seek_bar_progress));
        seekBar.setThumb(getResources().getDrawable(R.drawable.seek_bar_thumb_scrubber_control_selector_holo_dark));
        seekBarProgressChanger = new SeekBarProgressChangerRunnable(seekBar);
        visibilityRunnable = new PlayerUIVisibilityRunnable(playerControlsLayout);
        playerUIVisibilityHandler.postDelayed(visibilityRunnable, VISIBILITY_DELAY_MS);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setVisibility(preferences.getInt(PLAYER_UI_VISIBILITY, View.INVISIBLE));


        Button pauseButton = (Button) playerControlsLayout.findViewById(R.id.player_pause);
        Button previousButton = (Button) playerControlsLayout.findViewById(R.id.player_back);
        Button nextButton = (Button) playerControlsLayout.findViewById(R.id.player_next);
        Button settingsButton = (Button) playerControlsLayout.findViewById(R.id.player_settings);
        pauseButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
    }

    public void cleanUp(){

        if (seekBar != null ){
            seekBar.removeCallbacks(seekBarProgressChanger);
            seekBar.setProgress(0);
            seekBar.setMax(100);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void setSeekBarMax( long duration ){
        seekBar.setMax((int) duration);
        TextView playerFullTime = (TextView) playerControlsLayout.findViewById(R.id.player_time_full);
        playerFullTime.setText(TimeUtil.getTimeString(duration));
    }

    protected void changeSeekBarState (boolean startingPlaying, int progress){
        seekBar.setProgress(progress);
        handleSeekBarRunnable(!startingPlaying);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        TextView timeElapsed = (TextView) playerControlsLayout.findViewById(R.id.player_time_elapsed);
        if (playerControlsLayout.getVisibility() == View.VISIBLE) {
            timeElapsed.setText(TimeUtil.getTimeString(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "click");

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.isKioskMode()){
            return;
        }

        switch (v.getId()){
            case R.id.player_settings:
                onSettingsPressed();
                break;
            case R.id.player_back:
                onPlayerPrevious();
                break;
            case R.id.player_pause:
                paused = !paused;
                handleSeekBarRunnable(paused);
                if (paused) {
                    onPlayerPause();
                } else {
                    onPlayerPlay();
                }
                break;
            case R.id.player_next:
                onPlayerNext();
                break;
            default:
                changeVisibility();
                break;
        }
        playerUIVisibilityHandler.removeCallbacks(visibilityRunnable);
        if (playerControlsLayout.getVisibility() == View.VISIBLE) {
            playerUIVisibilityHandler.postDelayed(visibilityRunnable, VISIBILITY_DELAY_MS);
        }
    }

    private void changeVisibility(){
        int newVisibility = playerControlsLayout.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
        setVisibility(newVisibility);
    }
    private void setVisibility(int visibility){
        if (playerControlsLayout != null){
            playerControlsLayout.setVisibility(visibility);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putInt(PLAYER_UI_VISIBILITY,visibility).apply();
    }

    protected void setStatus(String status){
        TextView textView = (TextView) playerControlsLayout.findViewById(R.id.main_activity_status);
        textView.setText(status);
    }


    private void handleSeekBarRunnable(boolean isPausedNow){
        seekBar.removeCallbacks(seekBarProgressChanger);
        Button pauseButton = (Button) playerControlsLayout.findViewById(R.id.player_pause);
        if (isPausedNow) {
            Log.d(TAG, "PAUSED NOW");
            pauseButton.setBackground(getResources().getDrawable(R.drawable.player_play));
        } else {
            Log.d(TAG, "PLAYING NOW");
            seekBar.post(seekBarProgressChanger);
            pauseButton.setBackground(getResources().getDrawable(R.drawable.player_pause));
        }
    }

    public int getRemainingTime(){
        return seekBar.getMax() - seekBar.getProgress();
    }
}
