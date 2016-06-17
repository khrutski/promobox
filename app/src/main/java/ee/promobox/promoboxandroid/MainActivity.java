package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import ee.promobox.promoboxandroid.data.AppStatus;
import ee.promobox.promoboxandroid.data.CampaignFileType;
import ee.promobox.promoboxandroid.data.IPlayList;
import ee.promobox.promoboxandroid.data.PlayList;
import ee.promobox.promoboxandroid.data.PlayListItem;
import ee.promobox.promoboxandroid.data.Settings;
import ee.promobox.promoboxandroid.fragments.FragmentAudio;
import ee.promobox.promoboxandroid.fragments.FragmentImage;
import ee.promobox.promoboxandroid.fragments.FragmentMain;
import ee.promobox.promoboxandroid.fragments.FragmentRTP;
import ee.promobox.promoboxandroid.fragments.FragmentVideo;
import ee.promobox.promoboxandroid.fragments.FragmentWeb;
import ee.promobox.promoboxandroid.interfaces.FragmentPlaybackListener;


public class MainActivity extends Activity implements FragmentPlaybackListener, View.OnLongClickListener{

    public static final String CAMPAIGN_UPDATE = "ee.promobox.promoboxandroid.UPDATE";
    public static final String MAKE_TOAST = "ee.promobox.promoboxandroid.MAKE_TOAST";
    public static final String APP_START = "ee.promobox.promoboxandroid.START";
    public static final String UI_RESURRECT = "ee.promobox.promoboxandroid.RESURRECT";
    public static final String SET_STATUS = "ee.promobox.promoboxandroid.SET_STATUS";
    public static final String WRONG_UUID = "ee.promobox.promoboxandroid.WRONG_UUID";
    public static final String SETTINGS_CHANGE = "ee.promobox.promoboxandroid.SETTINGS_CHANGE";
    public static final String PLAY_SPECIFIC_FILE = "ee.promobox.promoboxandroid.PLAY_SPECIFIC_FILE";
    public static final String ERROR_MESSAGE = "Error %d , ( %s )";
    public static final String NO_ACTIVE_CAMPAIGN = "NO ACTIVE CAMPAIGN AT THE MOMENT";
    public static final String DEVICE_NOT_ACTIVE = "DEVICE NOT ACTIVE";
    public final static int RESULT_FINISH_FIRST_START = 2;
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    public static final int ORIENTATION_PORTRAIT_EMULATION = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    private AIDLInterface mainService;
    private IPlayList playList = new PlayList();
    private AppStatus appStatus = AppStatus.PLAYING;
    private boolean wrongUuid = false;
    private boolean mBound = false;
    private Timer timer = new Timer();

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            LOGGER.debug("onServiceConnected");

            mainService = AIDLInterface.Stub.asInterface(binder);
            updateAppStatus();

        }

        public void onServiceDisconnected(ComponentName className) {
            mainService = null;
        }
    };
    private BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mainService == null) {
                LOGGER.error("Main service don't work");
                return;
            }

            String action = intent.getAction();
            switch (action) {
                case CAMPAIGN_UPDATE:
                    updateAppStatus();
                    break;
                case MAKE_TOAST:
                    String toastString = intent.getStringExtra("Toast");
                    makeToast(toastString);
                    break;
                case PLAY_SPECIFIC_FILE:
                    playSpecificFile(intent);
                    break;
                case SET_STATUS:
                    updateAppStatus();
                    break;
                case WRONG_UUID:
                    wrongUuid();
                    break;
                case SETTINGS_CHANGE:
                    settingsChange(intent);
                    break;
            }
        }


    };

    private void hideSystemUI() {

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent start = new Intent();
        start.setAction(MainActivity.APP_START);
        sendBroadcast(start);

        setContentView(R.layout.activity_main);
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(CAMPAIGN_UPDATE);
        intentFilter.addAction(MAKE_TOAST);
        intentFilter.addAction(PLAY_SPECIFIC_FILE);
        intentFilter.addAction(SET_STATUS);
        intentFilter.addAction(WRONG_UUID);
        intentFilter.addAction(SETTINGS_CHANGE);

        this.getBaseContext().registerReceiver(bReceiver, intentFilter);

        getFragmentManager().beginTransaction().add(R.id.main_view, new FragmentMain()).commit();

        initWatchdogTimer();

    }

    private void initWatchdogTimer() {
        TimerTask serviceTimer = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (mainService != null) {
                        mainService.updateWatchdog();
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        };

        timer.schedule(serviceTimer, 3000, 3000);
    }

    private void startNextFile() {
        LOGGER.debug("startNextFile()");

        hideSystemUI();

        if (!playList.isEmpty() && appStatus == AppStatus.PLAYING) {

            PlayListItem nextFile = playList.current();

            LOGGER.debug("Next file from startNextFile = " + (nextFile != null ? nextFile.getCampaignFile().getType() : "null"));

            changeFragment(nextFile);
        }

    }

    private void changeFragment(PlayListItem nextFile) {

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        CampaignFileType fileType = nextFile != null ? nextFile.getCampaignFile().getType() : null;

        Fragment fragment = getFragmentByFileType(fileType);

        try {
            transaction.replace(R.id.main_view, fragment);
            transaction.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOGGER.debug(" onActivityResult() ,requestCode = " + requestCode);
        if (requestCode == RESULT_FINISH_FIRST_START) {
            try {
                wrongUuid = false;

                if (mainService != null && data != null) {
                    mainService.setUuid(data.getStringExtra("deviceUuid"));
                } else if (data == null) {
                    wrongUuid = true;
                    startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), RESULT_FINISH_FIRST_START);
                }


            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOGGER.debug("onResume");

        hideSystemUI();

        View view = findViewById(R.id.main_view);
        view.setOnLongClickListener(this);

        if (!mBound) {
            Intent intent = new Intent(this, MainService.class);
            bindService(intent, mConnection,
                    Context.BIND_AUTO_CREATE);

            mBound = true;
        }

        if (mainService != null) {

            if (getOrientation() == ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }


        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        startNextFile();

    }

    @Override
    protected void onPause() {
        super.onPause();
        LOGGER.debug("onPause");

        if (mainService != null) {
            try {
                mainService.setClosedNormally(true);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mainService != null) {
            try {
                mainService.setClosedNormally(false);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        timer.cancel();

        timer.purge();

        LOGGER.debug("onDestroy");

        this.getBaseContext().unregisterReceiver(bReceiver);

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        LOGGER.debug("Back pressed, do nothing");
    }

    public void makeToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
    }

    public int getOrientation() {
        int orintation = ORIENTATION_LANDSCAPE;

        if (mainService != null) {
            try {
                orintation = mainService.getOrientation();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return orintation;
    }

    private Fragment getFragmentByFileType(CampaignFileType fileType) {

        if (fileType == null) {
            return new FragmentMain();
        }

        switch (fileType) {
            case IMAGE:
                return new FragmentImage();
            case AUDIO:
                return new FragmentAudio();
            case VIDEO:
                return new FragmentVideo();
            case HTML:
                return new FragmentWeb();
            case RTP:
                return new FragmentRTP();
            default:
                return new FragmentMain();
        }
    }

    @Override
    public void onPlaybackStop() {
        LOGGER.debug("onPlaybackStop");
        startNextFile();
    }

    @Override
    public void onPlayBackRunnableError() {
        if (!playList.isEmpty()) {
            PlayListItem item = getPlayList().current();

            LOGGER.error("File with id {} in currentCampaigns with id {} is not playing normally, have to check it",
                    item.getCampaignFile().getId(), item.getCampaign().getCampaignId());
        }
        startNextFile();

    }

    @Override
    public boolean onLongClick(View view) {
        if (isKioskMode()) {
            return true;
        }

        Intent i = new Intent(MainActivity.this, SettingsActivity.class);

        Settings settings = null;

        try {
            settings = mainService.getSettings();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        i.putExtra("settings", settings);
        startActivity(i);
        return true;
    }

    public boolean isKioskMode() {
        try {
            return mainService.isKioskMode();
        } catch (Exception ex) {
            LOGGER.error(ex.toString(), ex);
        }

        return false;
    }

    public Optional<PlayListItem> play() {
        return playList.play();
    }

    public Optional<PlayListItem> getCurrentPlayListItem(CampaignFileType campaignFileType) {

        if (!getPlayList().isEmpty() && playList.current().getCampaignFile().getType() == campaignFileType) {
            PlayListItem file = getPlayList().current();

                try {
                    mainService.setCurrentFileId(file.getCampaignFile().getId());
                    mainService.setCurrentCampaignId(playList.currentCampaign().getCampaignId());

                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                return Optional.of(file);
        }

        return Optional.absent();
    }

    public void setPreviousFilePosition() {
        if (!playList.isEmpty()) {
            getPlayList().seek(getPlayList().previews());
        }
    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentById(R.id.main_view);
    }

    private void updateAppStatus(){
        try {
            appStatus = mainService.getAppStatus();
        } catch (Exception ex) {
            LOGGER.error(ex.toString(), ex);
        }

        if (appStatus != AppStatus.PLAYING) {
            setPlayList(new PlayList());
            if (!(getCurrentFragment() instanceof FragmentMain)) {
                changeFragment(null);
            }
            updateFragmentMainStatus();
        } else {
            try {
                setPlayList(new PlayList(mainService.getCurrentCampaigns()));
                startNextFile();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    public void updateFragmentMainStatus() {
        Fragment fragment = getCurrentFragment();

        if (fragment instanceof FragmentMain) {

            FragmentMain mainFragment = (FragmentMain)fragment;

            if (appStatus == AppStatus.DEVICE_NOT_ACTIVE) {
                mainFragment.updateStatus(AppStatus.DEVICE_NOT_ACTIVE, DEVICE_NOT_ACTIVE);
            } else if (appStatus == AppStatus.NO_ACTIVE_CAMPAIGN) {
                mainFragment.updateStatus(AppStatus.NO_ACTIVE_CAMPAIGN, NO_ACTIVE_CAMPAIGN);
            } else if (appStatus == AppStatus.DOWNLOADING) {
                mainFragment.updateStatus(AppStatus.DOWNLOADING, AppStatus.DOWNLOADING.name());
            } else {
                mainFragment.updateStatus(AppStatus.PLAYING, AppStatus.PLAYING.toString());
            }
        }
    }

    private void playSpecificFile(Intent intent) {
        int nextSpecificFileId = intent.getIntExtra("campaignFileId", -1);

        LOGGER.debug("PLAY_SPECIFIC_FILE with id {}", nextSpecificFileId);

        Optional<PlayListItem> campaignFileOptional = getPlayList().findPlayListItemByFileId(nextSpecificFileId);

        if (campaignFileOptional.isPresent()) {
            getPlayList().seek(campaignFileOptional.get());
            startNextFile();
        }
    }

    private void wrongUuid() {
        if (!wrongUuid) {
            wrongUuid = true;
            startActivityForResult(new Intent(MainActivity.this, FirstActivity.class), RESULT_FINISH_FIRST_START);
        }
    }

    private void settingsChange(Intent intent) {
        Settings settings = intent.getParcelableExtra("settings");

        try {
            mainService.setSettings(settings);
            makeToast("Settings saved");
            finish();
        } catch (Exception e) {
            makeToast("Could not set UUID, please try again later.");
        }
    }

    public IPlayList getPlayList() {
        return playList;
    }
    public void setPlayList(IPlayList playList) {
        this.playList = playList;
    }

}
