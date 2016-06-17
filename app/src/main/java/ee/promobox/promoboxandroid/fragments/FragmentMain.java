package ee.promobox.promoboxandroid.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.R;
import ee.promobox.promoboxandroid.data.AppStatus;
import ee.promobox.promoboxandroid.widgets.MyAnimatedDrawable;


public class FragmentMain extends Fragment {
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentMain.class);

    private MainActivity mainActivity;
    private View fragment_main_layout;


    private MyAnimatedDrawable downloadingAnimation;
    private MyAnimatedDrawable notActiveAnimationDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOGGER.debug("onCreateView");

        fragment_main_layout = inflater.inflate(R.layout.fragment_main, container, false);

        return fragment_main_layout;
    }

    @Override
    public void onDestroyView() {

        LOGGER.debug("onDestroyView");
        downloadingAnimation.recycleSelf();
        notActiveAnimationDrawable.recycleSelf();
        fragment_main_layout = null;
        downloadingAnimation = null;
        notActiveAnimationDrawable = null;
        mainActivity = null;
        super.onDestroyView();
    }

    @Override
    public void onAttach(final Activity activity) {
        LOGGER.debug("onAttach");
        mainActivity = (MainActivity) activity;

        notActiveAnimationDrawable = new MyAnimatedDrawable(mainActivity.getBaseContext(), MyAnimatedDrawable.ZZZ, 0, 0);
        downloadingAnimation = new MyAnimatedDrawable(mainActivity.getBaseContext(), MyAnimatedDrawable.DOWNLOADING, 0, 0);


        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        LOGGER.debug("onResume");

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
            fragment_main_layout.setRotation(270);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LOGGER.debug("onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void updateStatus(AppStatus appStatus, String status) {
        LOGGER.debug("updateStatus : " + status + " Enum = " + (appStatus != null ? appStatus.toString() : "null"));
        TextView textView = (TextView) fragment_main_layout.findViewById(R.id.main_activity_status);
        ImageView imageView = (ImageView) fragment_main_layout.findViewById(R.id.main_activity_status_image);
        if (textView != null) {
            textView.setText(status);
        }
        if (imageView != null) {
            AnimationDrawable newAnimationDrawable = null;

            if (appStatus != null) {
                switch (appStatus) {
                    case PLAYING:
                        LOGGER.debug("setting null animation");
                        break;
                    case DOWNLOADING:
                        LOGGER.debug("setting downloadingAnimation");
                        newAnimationDrawable = downloadingAnimation;
                        break;
                    case NO_ACTIVE_CAMPAIGN:
                        LOGGER.debug("setting ZZZ animation");
                        newAnimationDrawable = notActiveAnimationDrawable;
                        break;
                    case NO_FILES:
                        LOGGER.debug("setting ZZZ animation");
                        newAnimationDrawable = notActiveAnimationDrawable;
                        break;
                    case DEVICE_NOT_ACTIVE:
                        LOGGER.debug("setting ZZZ animation");
                        newAnimationDrawable = notActiveAnimationDrawable;
                        break;
                    default:
                        newAnimationDrawable = notActiveAnimationDrawable;
                }

                imageView.setImageDrawable(newAnimationDrawable);

                newAnimationDrawable.start();
            } else {
                LOGGER.debug("setting null animation");
            }

        }

    }

}
