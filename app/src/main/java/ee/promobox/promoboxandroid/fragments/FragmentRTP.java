package ee.promobox.promoboxandroid.fragments;

import android.app.Activity;
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
import android.widget.SeekBar;

import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.hls.DefaultHlsTrackSelector;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.hls.PtsTimestampAdjusterProvider;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.R;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.PlayListItem;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.util.PlayerLengthWatcher;
import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;


public class FragmentRTP extends FragmentWithSeekBar implements TextureView.SurfaceTextureListener,
        MediaCodecVideoTrackRenderer.EventListener, ExoPlayer.Listener
        , MediaCodecAudioTrackRenderer.EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentRTP.class);
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENTS = 128;


    private View fragmentVideoLayout;
    private TextureView videoView;

    private ExoPlayer exoPlayer;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private MediaCodecVideoTrackRenderer videoRenderer;

    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;

    private Handler videoLengthHandler = new Handler();
    private Runnable videoLengthStopper;

    private boolean textureAvailable;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOGGER.debug("onCreateView");
        fragmentVideoLayout = inflater.inflate(R.layout.fragment_video, container, false);
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
        textureAvailable = false;
        videoView.setSurfaceTextureListener(null);
        fragmentVideoLayout.setOnLongClickListener(null);
        fragmentVideoLayout.setOnClickListener(null);
        fragmentVideoLayout = null;
        videoView = null;
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

        videoLengthStopper = new PlayerLengthWatcher(this, playbackListener);

    }


    private void playNextFile() {
        mainActivity.play();
        play();
    }

    private void play() {
        Optional<PlayListItem> playListItem = mainActivity.getCurrentPlayListItem(CampaignFileType.RTP);

        if (playListItem.isPresent()) {
            cleanUp();
            if (textureAvailable) {
                playVideo(playListItem.get());
            }
        } else {
            playbackListener.onPlaybackStop();
        }
    }

    public void playVideo(PlayListItem playListItem) {
        try {

            cleanUp();

            final Surface surface = new Surface(videoView.getSurfaceTexture());

            LOGGER.debug("prepareStream() file = " + playListItem.getCampaignFile().getName());

            final Uri uri = Uri.parse(playListItem.getCampaignFile().getName());

            setStatus(playListItem.getCampaignFile().getName());

            exoPlayer = ExoPlayer.Factory.newInstance(2);

            HlsPlaylistParser parser = new HlsPlaylistParser();

            ManifestFetcher playlistFetcher = new ManifestFetcher<>(uri.toString(), new DefaultUriDataSource(getActivity(), "promobox"),
                    parser);

            playlistFetcher.singleLoad(exoPlayer.getPlaybackLooper(), new ManifestFetcher.ManifestCallback<HlsPlaylist>() {
                @Override
                public void onSingleManifest(HlsPlaylist hlsPlaylist) {
                    LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(BUFFER_SEGMENT_SIZE));
                    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                    DataSource dataSource = new DefaultUriDataSource(getActivity(), bandwidthMeter, "promobox");

                    HlsChunkSource chunkSource = new HlsChunkSource(true, dataSource, uri.toString(), hlsPlaylist,
                            DefaultHlsTrackSelector.newDefaultInstance(FragmentRTP.this.getActivity()), bandwidthMeter,
                            new PtsTimestampAdjusterProvider(), HlsChunkSource.ADAPTIVE_MODE_NONE);

                    HlsSampleSource sampleSource = new HlsSampleSource(chunkSource, loadControl,
                            BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE);

                    audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);
                    videoRenderer = new MediaCodecVideoTrackRenderer(getActivity(), sampleSource, MediaCodecSelector.DEFAULT,
                            MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000);

                    exoPlayer.prepare(videoRenderer, audioRenderer);
                    exoPlayer.addListener(FragmentRTP.this);
                    exoPlayer.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
                    exoPlayer.setPlayWhenReady(true);
                }

                @Override
                public void onSingleManifestError(IOException e) {
                    playbackListener.onPlaybackStop();
                }
            });

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            cleanUp();
            playNextFile();
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
            fragmentVideoLayout.setRotation(270);
        }
        if (textureAvailable) {
            play();
        }

    }

    @Override
    public void onPause() {
        LOGGER.debug("onPause()");
        super.onPause();
        cleanUp();
    }

    public void cleanUp() {
        super.cleanUp();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
            audioRenderer = null;
            videoRenderer = null;
        }

        videoLengthHandler.removeCallbacks(videoLengthStopper);
    }

    @Override
    public void onDestroy() {
        cleanUp();
        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        LOGGER.debug("onSurfaceTextureAvailable");
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

        if (playbackState == ExoPlayer.STATE_READY) {
            long duration = exoPlayer.getDuration();
            long position = exoPlayer.getCurrentPosition();
            LOGGER.debug("onPlayerStateChanged , exoPlayer.getDuration()" + duration);
            if (duration != ExoPlayer.UNKNOWN_TIME) {
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
        playbackListener.onPlaybackStop();
        LOGGER.error("onPlayerError");
    }

    private void makeToast(String toast) {
        mainActivity.makeToast(toast);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        long progress = seekBar.getProgress();
        if (exoPlayer != null) {
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
        LOGGER.debug(e.getMessage());
        playNextFile();
    }

    @Override
    public void onAudioTrackWriteError(AudioTrack.WriteException e) {
        LOGGER.error(e.getMessage(), e);
        playNextFile();
    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
        LOGGER.error("onAudioTrackUnderrun: bufferSize={}, bufferSizeMs={}, elapsedSinceLastFeedMs={}",
                bufferSize, bufferSizeMs, elapsedSinceLastFeedMs);
    }
}
