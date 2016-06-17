package ee.promobox.promoboxandroid.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SeekBar;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.R;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.PlayListItem;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;
import ee.promobox.promoboxandroid.widgets.FragmentWithSeekBar;


public class FragmentWeb extends FragmentWithSeekBar implements View.OnTouchListener{
    private long touchDown;

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentImage.class);

    private FragmentPlaybackListener playbackListener;
    private MainActivity mainActivity;

    private WebView webView;
    private View fragmentView;
    private String url;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_web,container,false);
        fragmentView.setOnLongClickListener(mainActivity);
        fragmentView.setOnClickListener(this);

        super.setView(fragmentView.findViewById(R.id.player_controls));

        webView = (WebView) fragmentView.findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(false);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.setWebViewClient(new MyWebViewClient());
        webView.setOnTouchListener(this);
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        LOGGER.debug("onDestroyView");
        fragmentView.setOnLongClickListener(null);
        fragmentView.setOnClickListener(null);
        webView.setOnTouchListener(null);

        fragmentView = null;
        webView = null;
        mainActivity = null;

        mainActivity = null;

        super.onDestroyView();
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
        webView.removeCallbacks(runnable);

        if (mainActivity.getOrientation() == MainActivity.ORIENTATION_PORTRAIT_EMULATION) {
            fragmentView.setRotation(270);
        }

        play();

    }

    private void play() {
        Optional<PlayListItem> playListItem = mainActivity.getCurrentPlayListItem(CampaignFileType.HTML);

        if (playListItem.isPresent()) {
            cleanUp();
            playWeb(playListItem.get());
        } else {
            playbackListener.onPlaybackStop();
        }
    }

    private void playNextFile(){
        mainActivity.play();
        play();

    }

    private void playWeb(PlayListItem campaignFile) {
        LOGGER.debug("playing " + campaignFile.getPath());
        setStatus(campaignFile.getCampaignFile().getName());
        int delay = mainActivity.getPlayList().currentCampaign().getDuration() * 1000;
        super.setSeekBarMax(delay);
        super.changeSeekBarState(true, 0);
        webView.postDelayed(runnable, delay);
        webView.loadUrl(campaignFile.getCampaignFile().getName());
        url = campaignFile.getCampaignFile().getName();

    }

    @Override
    public void onPause() {
        LOGGER.debug("onPause");
        webView.removeCallbacks(runnable);
        super.onPause();

    }



    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            playNextFile();
        }
    };

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        webView.removeCallbacks(runnable);
        webView.postDelayed(runnable, seekBar.getMax() - seekBar.getProgress());
    }

    @Override
    public void onPlayerPause() {
        webView.removeCallbacks(runnable);
    }

    @Override
    public void onPlayerPlay() {
        webView.removeCallbacks(runnable);
        webView.postDelayed(runnable, getRemainingTime());
    }

    @Override
    public void onPlayerPrevious() {
        webView.removeCallbacks(runnable);
        mainActivity.setPreviousFilePosition();
        play();
    }

    @Override
    public void onPlayerNext() {
        webView.removeCallbacks(runnable);
        playNextFile();
    }

    @Override
    public void onSettingsPressed() {
        fragmentView.performLongClick();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.equals(webView)){
            fragmentView.removeCallbacks(longClickRunnable);
            touchDown = 0;
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                touchDown = System.currentTimeMillis();
                fragmentView.postDelayed(longClickRunnable,1000);
            } else if (event.getAction() == MotionEvent.ACTION_UP){
                super.onClick(v);
            }
        }
        return v.onTouchEvent(event);
    }

    private class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String clickedHost = Uri.parse(url).getHost();
            String mineHost = Uri.parse(FragmentWeb.this.url).getHost();

            LOGGER.debug("Clicked host " + clickedHost);
            LOGGER.debug("Campaign host " + mineHost);

            String[] clickedArray = clickedHost.split("\\.");
            String[] mineArray = mineHost.split("\\.");
            String clicked = clickedArray[clickedArray.length-2];
            String mine = mineArray[mineArray.length-2];

            LOGGER.debug("Clicked host " + clickedHost + " , domain 2 lvl = " + clicked);
            LOGGER.debug("Campaign host " + mineHost + " , domain 2 lvl = " + mine);

            if (clicked.equals(mine)){
                webView.loadUrl(url);
            }

            return true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            view.loadData("<h1>No internet connection</h1>", "text/html; charset=UTF-8", null);
        }

    }

    private Runnable longClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (touchDown >= 1000){
                touchDown = 0;
                fragmentView.performLongClick();
            }
        }
    };
}
