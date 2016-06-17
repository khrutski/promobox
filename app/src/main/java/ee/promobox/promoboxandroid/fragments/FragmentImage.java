package ee.promobox.promoboxandroid.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.R;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.PlayListItem;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;

public class FragmentImage extends FragmentWithSeekBar {

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentImage.class);

    private ImageView slide;
    private View imageFragment;

    private FragmentPlaybackListener playbackListener;

    private MainActivity mainActivity;
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            playNextFile();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.debug("onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOGGER.debug("onCreateView");
        imageFragment = inflater.inflate(R.layout.fragment_image, container, false);
        imageFragment.setOnLongClickListener(mainActivity);
        imageFragment.setOnClickListener(this);
        super.setView(imageFragment.findViewById(R.id.player_controls));

        slide = (ImageView) imageFragment.findViewById(R.id.slide_1);
        return imageFragment;
    }

    @Override
    public void onDestroyView() {
        LOGGER.debug("onDestroyView");
        imageFragment.setOnLongClickListener(null);
        imageFragment.setOnClickListener(null);
        imageFragment = null;
        slide.destroyDrawingCache();
        slide = null;
        super.onDestroyView();
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        LOGGER.debug("onInflate");
        super.onInflate(activity, attrs, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        LOGGER.debug("onAttach");
        super.onAttach(activity);
        playbackListener = (FragmentPlaybackListener) activity;
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onResume() {
        LOGGER.debug("onResume");
        super.onResume();
        slide.removeCallbacks(runnable);

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
            imageFragment.setRotation(270);
        }

        play();

    }

    @Override
    public void onPause() {
        LOGGER.debug("onPause");
        slide.removeCallbacks(runnable);
        recycleBitmap();
        slide.setImageDrawable(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        LOGGER.debug("onDestroy");
        super.onDestroy();
    }

    private Bitmap decodeBitmap(File file) {
        Bitmap bm = null;

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inDither = false;
            options.inTempStorage = new byte[32 * 1024];

            bm = BitmapFactory.decodeFile(file.getPath(), options);


        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            makeToast(String.format(
                    MainActivity.ERROR_MESSAGE, 21, ex.getClass().getSimpleName()));
        }

        return bm;
    }

    private void playNextFile(){
        mainActivity.play();
        play();

    }

    private void play() {
        Optional<PlayListItem> playListItem = mainActivity.getCurrentPlayListItem(CampaignFileType.IMAGE);

        if (playListItem.isPresent()) {
            cleanUp();
            playImage(playListItem.get());
        } else {
            playbackListener.onPlaybackStop();
        }
    }


    private void playImage(PlayListItem playListItem) {
        String path = playListItem.getPath();
        File file = new File(path);
        if (!file.exists()){
            String message = " No file in path " + path;
            LOGGER.error(message);
            makeToast(message);
            slide.postDelayed(runnable, 1000);
            return;
        }

        try {
            Bitmap bitmap = decodeBitmap(file);
            recycleBitmap();
            slide.setImageBitmap(bitmap);
            setStatus(playListItem.getCampaignFile().getName());
            int delay = playListItem.getCampaign().getDuration() * 1000;
            super.setSeekBarMax(delay);
            super.changeSeekBarState(true, 0);
            slide.postDelayed(runnable, delay);


        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            LOGGER.error("Path = " + path);

            makeToast(String.format(
                    MainActivity.ERROR_MESSAGE, 22, ex.getClass().getSimpleName()));

            playNextFile();
        }

    }


    private void recycleBitmap() {
        BitmapDrawable toRecycle = (BitmapDrawable) (slide != null ? slide.getDrawable(): null);

        if (toRecycle != null && toRecycle.getBitmap() != null) {
            toRecycle.getBitmap().recycle();
        }
    }

    private void makeToast(String toast){
        mainActivity.makeToast(toast);
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        slide.removeCallbacks(runnable);
        slide.postDelayed(runnable, seekBar.getMax() - seekBar.getProgress());
    }

    @Override
    public void onPlayerPause() {
        slide.removeCallbacks(runnable);

    }

    @Override
    public void onPlayerPlay() {
        slide.removeCallbacks(runnable);
        slide.postDelayed(runnable, getRemainingTime());
    }

    @Override
    public void onPlayerPrevious() {
        slide.removeCallbacks(runnable);
        mainActivity.setPreviousFilePosition();
        play();
    }

    @Override
    public void onPlayerNext() {
        slide.removeCallbacks(runnable);
        playNextFile();
    }

    @Override
    public void onSettingsPressed() {
        imageFragment.performLongClick();
    }
}
