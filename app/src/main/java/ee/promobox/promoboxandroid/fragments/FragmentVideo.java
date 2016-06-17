package ee.promobox.promoboxandroid.fragments;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.common.base.Optional;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.R;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.PlayListItem;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.util.PlayerLengthWatcher;
import ee.promobox.promoboxandroid.util.VideoMatrixCalculator;
import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;


public class FragmentVideo extends FragmentWithSeekBar implements TextureView.SurfaceTextureListener,
        MediaCodecVideoTrackRenderer.EventListener , ExoPlayer.Listener
        , MediaCodecAudioTrackRenderer.EventListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentVideo.class);

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENTS = 64;


    private View fragmentVideoLayout;
    private TextureView videoView;

    private ExoPlayer exoPlayer;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private MediaCodecVideoTrackRenderer videoRenderer;

    private int viewOriginalHeight = 0;
    private int viewOriginalWidth = 0;
    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;

    private Handler videoLengthHandler = new Handler();
    private Handler textureChecker = new Handler();
    private PlayerLengthWatcher videoLengthStopper;

    private boolean textureAvailable;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOGGER.debug("onCreateView");
        fragmentVideoLayout =  inflater.inflate(R.layout.fragment_video,container,false);
        super.setView(fragmentVideoLayout.findViewById(R.id.player_controls));
        videoView = (TextureView) fragmentVideoLayout.findViewById(R.id.video_texture_view);
        videoView.setSurfaceTextureListener(this);
        fragmentVideoLayout.setOnLongClickListener(mainActivity);
        fragmentVideoLayout.setOnClickListener(this);
        return fragmentVideoLayout;
    }

    @Override
    public void onDestroyView() {
        LOGGER.debug("onDestroyView");
        fragmentVideoLayout.setOnLongClickListener(null);
        fragmentVideoLayout.setOnClickListener(null);
        videoView.setSurfaceTextureListener(null);
        videoLengthHandler.removeCallbacks(videoLengthStopper);
        if (videoLengthStopper != null) {
            videoLengthStopper.clear();
        }
        textureChecker.removeCallbacksAndMessages(null);
        videoView = null;
        fragmentVideoLayout = null;
        textureAvailable = false;
        exoPlayer = null;
        mainActivity = null;

        super.onDestroyView();
    }



    @Override
    public void onAttach(Activity activity) {
        LOGGER.debug("onAttach");
        super.onAttach(activity);
        playbackListener = (FragmentPlaybackListener) activity;
        mainActivity = (MainActivity) activity;

        videoLengthStopper = new PlayerLengthWatcher(this,playbackListener);

    }


    private void playNextFile(){
        mainActivity.play();
        play();
    }

    private void play() {
        cleanUp();

        Optional<PlayListItem> playListItem = mainActivity.getCurrentPlayListItem(CampaignFileType.VIDEO);

        if (playListItem.isPresent()) {
            if (textureAvailable) {
                playVideo(playListItem.get());
            } else {
                waitTextureOrExit();
            }
        } else {
            playbackListener.onPlaybackStop();
        }
    }

    private void waitTextureOrExit() {
        textureChecker.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!textureAvailable) {
                    LOGGER.error("Texture isn't available in 5s");

                    if (mainActivity != null) {
                        mainActivity.finish();
                        System.exit(0);
                    }
                }
            }
        }, 5000);
    }

    public void playVideo(PlayListItem playListItem) {
        try {
            cleanUp();
            Surface surface = new Surface(videoView.getSurfaceTexture());
            String pathToFile = playListItem.getPath();

            if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
                Point videoSize = VideoMatrixCalculator.calculateVideoSize(pathToFile);
                videoSize = VideoMatrixCalculator.calculateNeededVideoSize(videoSize,viewOriginalHeight,viewOriginalWidth);
                RelativeLayout.LayoutParams relPar = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
                relPar.width = videoSize.x;
                relPar.height = videoSize.y;
                videoView.setLayoutParams(relPar);
            }

            LOGGER.debug("prepareVideo() file = " + FilenameUtils.getBaseName(pathToFile));
            LOGGER.debug(pathToFile);

            Uri uri = Uri.parse(pathToFile);

            setStatus(playListItem.getCampaignFile().getName());

            Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);

            DataSource dataSource = new DefaultUriDataSource(getActivity(), null, "promobox");

            SampleSource source = new ExtractorSampleSource(uri, dataSource, allocator, BUFFER_SEGMENT_SIZE * BUFFER_SEGMENTS, new Mp4Extractor());

            audioRenderer = new MediaCodecAudioTrackRenderer(source, MediaCodecSelector.DEFAULT);
            videoRenderer = new MediaCodecVideoTrackRenderer(getActivity(), source, MediaCodecSelector.DEFAULT,
                    MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000);
            exoPlayer = ExoPlayer.Factory.newInstance(2);
            exoPlayer.prepare(videoRenderer, audioRenderer);
            exoPlayer.addListener(this);
            exoPlayer.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
            exoPlayer.setPlayWhenReady(true);

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);

            makeToast(String.format(
                    MainActivity.ERROR_MESSAGE, 41, ex.getClass().getSimpleName()));

            playNextFile();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        cleanUp();

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION){
            fragmentVideoLayout.setRotation(270);
        }

        play();
    }

    @Override
    public void onPause() {
        LOGGER.debug("onPause()");
        super.onPause();
        cleanUp();
    }

    public void cleanUp() {
        super.cleanUp();

        LOGGER.debug("Fragment vdieo: cleanUp");

        if(exoPlayer != null){
            exoPlayer.removeListener(this);
            exoPlayer.release();
            exoPlayer = null;
            audioRenderer = null;
            videoRenderer = null;
        }

        videoLengthHandler.removeCallbacks(videoLengthStopper);
        textureChecker.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        cleanUp();
        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        LOGGER.debug("onSurfaceTextureAvailable");
        viewOriginalHeight =  videoView.getMeasuredHeight();
        viewOriginalWidth =  videoView.getMeasuredWidth();
        textureAvailable = true;
        play();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        LOGGER.debug("onSurfaceTextureDestroyed");
        textureAvailable = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
        makeToast("Video player decoder initialization error");
        LOGGER.debug("onDecoderInitializationError");
        cleanUp();
        playNextFile();

    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
        makeToast("Video player crypto error");
        LOGGER.debug("onCryptoError");
        cleanUp();
        playNextFile();
    }

    @Override
    public void onDecoderInitialized(String s, long l, long l1) {

    }

    @Override
    public void onDroppedFrames(int i, long l) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }


    @Override
    public void onDrawnToSurface(Surface surface) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == ExoPlayer.STATE_READY){
            long duration = exoPlayer.getDuration();
            long position = exoPlayer.getCurrentPosition();
            LOGGER.debug("onPlayerStateChanged , exoPlayer.getDuration()" + duration);
            if (duration != ExoPlayer.UNKNOWN_TIME){
                super.setSeekBarMax(duration);
                if (playWhenReady) {
                    super.changeSeekBarState(playWhenReady, (int) exoPlayer.getCurrentPosition());
                    videoLengthHandler.postDelayed(videoLengthStopper, duration - position + 10 * 1000);
                }
            }
        }

        if (playbackState == ExoPlayer.STATE_ENDED) {
            playNextFile();
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
    }

    @Override
    public void onPlayerError(ExoPlaybackException ex) {
        makeToast("Player ERROR ");
        LOGGER.debug("onPlayerError");
        cleanUp();
        playNextFile();
    }

    private void makeToast(String toast){
        mainActivity.makeToast(toast);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long progress = seekBar.getProgress();
        if (exoPlayer != null){
            onPlayerPause();
            exoPlayer.seekTo(progress);
            onPlayerPlay();

            videoLengthHandler.removeCallbacks(videoLengthStopper);
            videoLengthHandler.postDelayed(videoLengthStopper, seekBar.getMax() - progress + 10 * 1000);
        }
    }

    @Override
    public void onPlayerPause() {
        LOGGER.debug("onPlayerPause");
        videoLengthHandler.removeCallbacks(videoLengthStopper);
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onPlayerPlay() {
        LOGGER.debug("onPlayerPlay");

        videoLengthHandler.postDelayed(videoLengthStopper, exoPlayer.getDuration() - exoPlayer.getCurrentPosition() + 3 * 1000);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onPlayerPrevious() {
        cleanUp();
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
        fragmentVideoLayout.performLongClick();
    }


    @Override
    public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
        LOGGER.error(e.getMessage(), e);
        cleanUp();
        playNextFile();
    }

    @Override
    public void onAudioTrackWriteError(AudioTrack.WriteException e) {
        LOGGER.error(e.getMessage(), e);
        cleanUp();
        playNextFile();
    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
        LOGGER.error("onAudioTrackUnderrun: bufferSize={}, bufferSizeMs={}, elapsedSinceLastFeedMs={}",
                bufferSize, bufferSizeMs, elapsedSinceLastFeedMs);

    }
}
