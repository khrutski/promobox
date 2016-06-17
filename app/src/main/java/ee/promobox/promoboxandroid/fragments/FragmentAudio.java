package ee.promobox.promoboxandroid.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.SampleSource;
import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.R;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.PlayListItem;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.util.PlayerLengthWatcher;
import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;
import ee.promobox.promoboxandroid.widgets.MyAnimatedDrawable;


public class FragmentAudio extends FragmentWithSeekBar implements ExoPlayer.Listener{

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentAudio.class);
    private static Runnable audioLengthStopper;
    private ExoPlayer exoPlayer;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;
    private Handler audioLengthHandler = new Handler();
    private View audioView;
    private MyAnimatedDrawable audioAnimation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOGGER.debug("onCreateView");
        audioView = inflater.inflate(R.layout.fragment_audio, container, false);
        audioAnimation = new MyAnimatedDrawable(mainActivity, MyAnimatedDrawable.AUDIO, 0, 0);
        audioView.setBackground(audioAnimation);
        audioAnimation.start();
        super.setView(audioView.findViewById(R.id.player_controls));
        audioView.setOnLongClickListener(mainActivity);
        audioView.setOnClickListener(this);

        return audioView;
    }

    @Override
    public void onDestroyView() {
        LOGGER.debug("onDestroyView");
        audioView.setOnLongClickListener(null);
        audioView.setOnClickListener(null);
        audioAnimation = null;
        audioView = null;
        exoPlayer = null;
        mainActivity = null;

        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        LOGGER.debug("onAttach");
        super.onAttach(activity);
        playbackListener = (FragmentPlaybackListener) activity;

        audioLengthStopper = new PlayerLengthWatcher(this,playbackListener);

        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onResume() {
        LOGGER.debug("onResume");
        super.onResume();

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
            audioView.setRotation(270);
        }

        play();
    }

    @Override
    public void onDestroy() {
        cleanUp();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        LOGGER.debug("onPause");
        super.onPause();
        cleanUp();
    }

    private void playNextFile(){
        mainActivity.play();
        play();

    }

    private void play() {
        Optional<PlayListItem> playListItem = mainActivity.getCurrentPlayListItem(CampaignFileType.AUDIO);

        if (playListItem.isPresent()) {
            cleanUp();
            playAudio(playListItem.get());
        } else {
            playbackListener.onPlaybackStop();
        }
    }


    private void playAudio(PlayListItem playListItem) {
        cleanUp();
        String pathToFile = playListItem.getPath();
        File file = new File(pathToFile);
        if (file.exists()) {
            LOGGER.debug("playAudio() file = " + file.getName() + " PATH = " + pathToFile);

            setStatus(playListItem.getCampaignFile().getName());

            Uri uri = Uri.parse(pathToFile);

            SampleSource source = new FrameworkSampleSource(getActivity(), uri, null);

            audioRenderer = new MediaCodecAudioTrackRenderer(source, MediaCodecSelector.DEFAULT);

            exoPlayer = ExoPlayer.Factory.newInstance(1);
            exoPlayer.prepare(audioRenderer);
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.addListener(this);
        }
    }

    public void cleanUp() {
        super.cleanUp();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        audioRenderer = null;

        audioLengthHandler.removeCallbacks(audioLengthStopper);

    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_PREPARING){
            LOGGER.debug("ExoPlayer.STATE_PREPARING ");
        }
        if (playbackState == ExoPlayer.STATE_READY){
            long duration = exoPlayer.getDuration();
            long position = exoPlayer.getCurrentPosition();
            LOGGER.debug("ExoPlayer.STATE_READY , exoPlayer.getDuration()" + duration);
            if (duration != ExoPlayer.UNKNOWN_TIME){
                super.setSeekBarMax( duration );
                if (playWhenReady) {
                    audioAnimation.start();
                    super.changeSeekBarState(playWhenReady,(int) exoPlayer.getCurrentPosition());
                    audioLengthHandler.postDelayed(audioLengthStopper, duration - position + 10 * 1000);
                }
            }
        }
        if (playbackState == ExoPlayer.STATE_ENDED) {
            playNextFile();
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
//        Log.d(TAG, "onPlayWhenReadyCommitted");
    }

    @Override
    public void onPlayerError(ExoPlaybackException ex) {
        mainActivity.makeToast("Audio player error");

        LOGGER.debug("onPlayerError " + ex.getMessage());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                playNextFile();
            }
        }, 1000);
    }



    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long progress = seekBar.getProgress();
        if (exoPlayer != null){
            exoPlayer.seekTo(progress);

            audioLengthHandler.removeCallbacks(audioLengthStopper);
            audioLengthHandler.postDelayed(audioLengthStopper, seekBar.getMax()-progress + 3 * 1000);
        }
    }

    @Override
    public void onPlayerPause() {
        LOGGER.debug("onPlayerPause");
        audioAnimation.stop();
        audioLengthHandler.removeCallbacks(audioLengthStopper);
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onPlayerPlay() {
        LOGGER.debug("onPlayerPlay");

        audioLengthHandler.postDelayed(audioLengthStopper, exoPlayer.getDuration()-exoPlayer.getCurrentPosition() + 3 * 1000);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onPlayerPrevious() {
        mainActivity.setPreviousFilePosition();
        play();
    }

    @Override
    public void onPlayerNext() {
        cleanUp();
        playNextFile();
    }

    @Override
    public void onSettingsPressed() {
        audioView.performLongClick();
    }


}
