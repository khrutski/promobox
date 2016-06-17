package ee.promobox.promoboxandroid.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import ee.promobox.promoboxandroid.MainActivity;


public class AppState {
    private int orientation = MainActivity.ORIENTATION_LANDSCAPE;
    private int currentFileId;
    private int currentCampaignId;


    private Campaign loadingCampaign = new Campaign();
    private int loadingCampaignProgress;

    private List<Integer> deviceWorkDays = new ArrayList<>();

    private LocalTime workHourFrom = LocalTime.now();
    private LocalTime workHourTo = LocalTime.now();

    private boolean kioskMode = false;

    private boolean firstStart = true;
    private boolean closedNormally = false;
    private boolean working;

    private AppStatus status = AppStatus.PLAYING;

    @JsonIgnore
    private Settings settings = new Settings();

    private List<Campaign> currentCampaigns = new ArrayList<>();

    private List<Campaign> campaigns = new ArrayList<>();

    private DateTime lastUpdateDate = new DateTime();


    public AppState(){

    }


    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getCurrentFileId() {
        return currentFileId;
    }

    public void setCurrentFileId(int currentFileId) {
        this.currentFileId = currentFileId;
    }

    public Campaign getLoadingCampaign() {
        return loadingCampaign;
    }

    public void setLoadingCampaign(Campaign loadingCampaign) {
        this.loadingCampaign = loadingCampaign;
    }

    public int getLoadingCampaignProgress() {
        return loadingCampaignProgress;
    }

    public void setLoadingCampaignProgress(int loadingCampaignProgress) {
        this.loadingCampaignProgress = loadingCampaignProgress;
    }

    public List<Integer> getDeviceWorkDays() {
        return deviceWorkDays;
    }

    public void setDeviceWorkDays(List<Integer> deviceWorkDays) {
        this.deviceWorkDays = deviceWorkDays;
    }

    public LocalTime getWorkHourFrom() {
        return workHourFrom;
    }

    public void setWorkHourFrom(LocalTime workHourFrom) {
        this.workHourFrom = workHourFrom;
    }

    public LocalTime getWorkHourTo() {
        return workHourTo;
    }

    public void setWorkHourTo(LocalTime workHourTo) {
        this.workHourTo = workHourTo;
    }

    public boolean isKioskMode() {
        return kioskMode;
    }

    public void setKioskMode(boolean kioskMode) {
        this.kioskMode = kioskMode;
    }

    public boolean isFirstStart() {
        return firstStart;
    }

    public void setFirstStart(boolean firstStart) {
        this.firstStart = firstStart;
    }

    public boolean isClosedNormally() {
        return closedNormally;
    }

    public void setClosedNormally(boolean closedNormally) {
        this.closedNormally = closedNormally;
    }

    public AppStatus getStatus() {
        return status;
    }

    public void setStatus(AppStatus status) {
        this.status = status;
    }

    public List<Campaign> getCurrentCampaigns() {
        return currentCampaigns;
    }

    public void setCurrentCampaigns(List<Campaign> currentCampaigns) {
        this.currentCampaigns = currentCampaigns;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    public DateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(DateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public int getCurrentCampaignId() {
        return currentCampaignId;
    }

    public void setCurrentCampaignId(int currentCampaignId) {
        this.currentCampaignId = currentCampaignId;
    }
}
