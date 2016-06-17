package ee.promobox.promoboxandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ee.promobox.promoboxandroid.data.AppState;
import ee.promobox.promoboxandroid.data.AppStatus;
import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.Settings;
import ee.promobox.promoboxandroid.intents.StatusIntent;
import ee.promobox.promoboxandroid.service.FileService;
import ee.promobox.promoboxandroid.service.IFileService;
import ee.promobox.promoboxandroid.service.INetworkService;
import ee.promobox.promoboxandroid.service.NetworkService;
import ee.promobox.promoboxandroid.service.PullRequest;
import ee.promobox.promoboxandroid.service.PullResponse;
import ee.promobox.promoboxandroid.util.AppStateHelper;
import ee.promobox.promoboxandroid.util.InternetConnectionUtil;
import ee.promobox.promoboxandroid.util.SettingsSavingException;
import ee.promobox.promoboxandroid.util.TimeUtil;
import ee.promobox.promoboxandroid.util.error.DeviceNotFoundError;


public class MainService extends Service {

    public final static String DEFAULT_SERVER = "http://api.promobox.ee";

    private static final Logger LOGGER = LoggerFactory.getLogger(MainService.class);
    private final Watchdog watchdog = new Watchdog();
    private AIDLInterface.Stub mBinder = new MainServiceAIDL(this);
    private AppState appState;
    private INetworkService networkService;
    private IFileService fileService;
    private Timer timer = new Timer();


    @Override
    public void onCreate() {
        LOGGER.debug("onCreate()");

        fileService = new FileService();

        setAppState(fileService.restoreAppState());

        appState.setStatus(AppStatus.PLAYING);

        selectActiveCampaigns();

        networkService = new NetworkService(getAppState());

        watchdog.update();

        initTimer();
    }

    private void initTimer() {
        LOGGER.info("Init timer");

        timer.cancel();
        timer.purge();

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                appState.setLastUpdateDate(new DateTime());
                checkRepeat(false);
            }
        }, 0, 15000);
    }

    @Override
    public void onDestroy() {
        LOGGER.info("onDestroy");
        super.onDestroy();

        timer.cancel();
        timer.purge();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGGER.debug("Start command");

        boolean startMainActivity  =  intent != null && intent.getBooleanExtra("startMainActivity", false);
        boolean serviceAlarm = intent != null && intent.getBooleanExtra("serviceAlarm", false);

        if (!serviceAlarm) {
            checkRepeat(startMainActivity);
        } else {
            LOGGER.info("Alarm call");
            if (TimeUtil.isIncorrectTimePeriod(appState.getLastUpdateDate())) {
                LOGGER.error("Incorrect time, re-schedule tasks");
                initTimer();
            }
        }

        return Service.START_STICKY;
    }

    private void checkRepeat(boolean startMainActivity) {
        if (InternetConnectionUtil.isNetworkConnected(this)) {
            checkAndDownloadCampaign();
        } else {
            selectActiveCampaigns();
            LOGGER.debug("No internet connection");
        }

        if (startMainActivity || !watchdog.isOK()) {
            LOGGER.debug("Main activity started");
            startMainActivity();
        }
    }

    public void setSettings(Settings settings) {
        try {
            fileService.setSettings(settings);
        } catch (SettingsSavingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


    public void checkAndDownloadCampaign() {
        LOGGER.debug("checkAndDownloadCampaign()");

        if (getAppState().getSettings().getUuid() != null &&
                !"fail".equals(getAppState().getSettings().getUuid())) {

            PullRequest pullRequest = new PullRequest();

            pullRequest.setUuid(appState.getSettings().getUuid());
            pullRequest.setForce(true);
            pullRequest.setIp(new ArrayList<String>());
            pullRequest.setCurrentFileId(appState.getCurrentFileId());
            pullRequest.setCurrentCampaignId(appState.getCurrentCampaignId());
            pullRequest.setFreeSpace(FileService.freeSpace());
            pullRequest.setCache(FileUtils.sizeOfDirectory(FileService.ROOT.getAbsoluteFile()));
            pullRequest.setVersion(BuildConfig.VERSION_CODE);
            pullRequest.setIsOnTop(watchdog.isOK());

            if (appState.getStatus() == AppStatus.DOWNLOADING) {
                pullRequest.setLoadingCampaignId(appState.getLoadingCampaign().getCampaignId());
                pullRequest.setLoadingCampaignProgress(appState.getLoadingCampaignProgress());
                pullRequest.setCurrentFileId(0);
                pullRequest.setCurrentCampaignId(0);
            }

            ListenableFuture<PullResponse> pullResponse = networkService.pullRequest(pullRequest);

            Futures.addCallback(pullResponse, new FutureCallback<PullResponse>() {
                        @Override
                        public void onSuccess(PullResponse result) {
                            checkUpdate(result);
                            checkCommand(result);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            if (t instanceof DeviceNotFoundError) {
                                Intent intent = new Intent(MainActivity.WRONG_UUID);
                                sendBroadcast(intent);
                            }

                            LOGGER.error(t.getMessage(), t);
                        }
                    }
            );
        } else {
            Intent update = new Intent(MainActivity.WRONG_UUID);
            sendBroadcast(update);
        }

    }

    private void checkCommand(PullResponse result) {
        if (result.isClearCache()) {
            LOGGER.info("Clear cache");
            fileService.clearCache(appState.getCampaigns());
        }

        if (result.isOpenApp()) {
            LOGGER.info("Open app");
            startMainActivity();
        }

        if (result.isRestart()) {
            LOGGER.info("Restart");
            try {
                Runtime.getRuntime().exec(new String[]{"su","-c","reboot now"});
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        if (result.getNextFile() != null) {
            LOGGER.info("Next file");
            CampaignFile campaignFile = AppStateHelper.findCampaignFile(result.getNextFile(),
                    appState.getCurrentCampaigns());

            if (campaignFile != null ) {
                Intent intent = new Intent(MainActivity.PLAY_SPECIFIC_FILE);
                intent.putExtra("campaignFileId", campaignFile.getId());
                sendBroadcast(intent);
            }
        }

    }

    private void checkUpdate(PullResponse result) {
        if (AppStateHelper.isAppStateChanged(appState, result)) {

            LOGGER.info("App state changed");

            List<Campaign> list = AppStateHelper.updatedCampaigns(appState, result);

            downloadCampaigns(list);

            AppStateHelper.merge(appState, result);

            fileService.saveAppState(appState);

            selectActiveCampaigns();

            Intent update = new Intent(MainActivity.CAMPAIGN_UPDATE);
            sendBroadcast(update);

        } else {
            if (!tryDownloadAbsentFiles()) {
                selectActiveCampaigns();
            }
        }
    }


    private boolean tryDownloadAbsentFiles() {
        if (appState.getStatus() == AppStatus.PLAYING) {
            List<Campaign> notCompleteDownloadedCampaigns = new ArrayList<>();

            for (Campaign campaign : appState.getCurrentCampaigns()) {
                for (CampaignFile campaignFile : campaign.getFiles()) {
                    if (campaignFile.getType().isDownloadable()) {
                        File file = FileService.getFile(campaign, campaignFile);
                        if (!file.exists()) {
                            LOGGER.info("Downloading file: {}", campaignFile.toString());
                            notCompleteDownloadedCampaigns.add(campaign);
                        }
                    }
                }
            }

            if (!notCompleteDownloadedCampaigns.isEmpty()) {
                downloadCampaigns(notCompleteDownloadedCampaigns);
                return true;
            }
        }

        return false;
    }

    private void downloadCampaigns(List<Campaign> campaigns) {
        LOGGER.debug("Download campaigns {}", campaigns);

        appState.setStatus(AppStatus.DOWNLOADING);

        sendBroadcast(new StatusIntent(appState.getStatus(), appState.getStatus().toString()));

        Futures.addCallback(networkService.downloadCampaigns(campaigns), new FutureCallback<List<File>>() {
            @Override
            public void onSuccess(List<File> result) {
                LOGGER.debug("Downloading complete");
                appState.setStatus(AppStatus.NO_ACTIVE_CAMPAIGN);
                selectActiveCampaigns();
            }

            @Override
            public void onFailure(Throwable t) {
                LOGGER.debug("Downloading failed");
                LOGGER.error(t.getMessage(), t);
                appState.setStatus(AppStatus.NO_ACTIVE_CAMPAIGN);
                selectActiveCampaigns();
            }
        });
    }


    public void selectActiveCampaigns() {
        LOGGER.debug("selectActiveCampaigns()");

        if (TimeUtil.checkDeviceActive(appState)) {

            List<Campaign> campaignsToSetCurrent = new ArrayList<>();

            for(Campaign camp: getAppState().getCampaigns()) {

                LOGGER.debug("Date start: {}", camp.getStartDate().toString());
                LOGGER.debug("Date end: {}", camp.getEndDate().toString());

                if (TimeUtil.hasToBePlayed(camp)) {
                    LOGGER.debug("Date bounds for currentCampaign: " + camp.getCampaignName());
                    campaignsToSetCurrent.add(camp);
                }
            }

            getAppState().setCurrentCampaigns(campaignsToSetCurrent);

            updateStatusOnActiveDevice();

        } else if (appState.getStatus() != AppStatus.DOWNLOADING) {
            getAppState().setCurrentCampaigns(new ArrayList<Campaign>());
            getAppState().setCurrentCampaignId(0);
            getAppState().setCurrentFileId(0);

            appState.setStatus(AppStatus.DEVICE_NOT_ACTIVE);
            LOGGER.debug("Sending status " + AppStatus.DEVICE_NOT_ACTIVE);
            sendBroadcast(new StatusIntent(AppStatus.DEVICE_NOT_ACTIVE, MainActivity.DEVICE_NOT_ACTIVE));
        }

    }

    private void updateStatusOnActiveDevice() {
        if (appState.getStatus() == AppStatus.PLAYING) {
            if (getAppState().getCurrentCampaigns().isEmpty()) {
                appState.setStatus(AppStatus.NO_ACTIVE_CAMPAIGN);
                appState.setCurrentFileId(0);
                appState.setCurrentCampaignId(0);
                sendBroadcast(new StatusIntent(appState.getStatus(), appState.getStatus().toString()));
            }
        }

        if (appState.getStatus() != AppStatus.PLAYING && appState.getStatus() != AppStatus.DOWNLOADING) {

            if (getAppState().getCurrentCampaigns().isEmpty()) {
                appState.setStatus(AppStatus.NO_ACTIVE_CAMPAIGN);
            } else {
                appState.setStatus(AppStatus.PLAYING);
            }

            sendBroadcast(new StatusIntent(appState.getStatus(), appState.getStatus().toString()));
        }
    }

    public void startMainActivity(){
        Intent mainActivity = new Intent(getBaseContext(), MainActivity.class);

        mainActivity.setAction(Intent.ACTION_MAIN);
        mainActivity.addCategory(Intent.CATEGORY_LAUNCHER);
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);

        getApplication().startActivity(mainActivity);
    }

    public void setClosedNormally(boolean closedNormally) {
        getAppState().setClosedNormally(closedNormally);

        if (!closedNormally) {
            startMainActivity();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public AppState getAppState() {
        return appState;
    }

    public void setAppState(AppState appState) {
        this.appState = appState;
    }

    public void updateWatchdog() {
        watchdog.update();
    }
}
