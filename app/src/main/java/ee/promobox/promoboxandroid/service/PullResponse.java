package ee.promobox.promoboxandroid.service;


import com.fasterxml.jackson.annotation.JsonInclude;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import ee.promobox.promoboxandroid.data.Campaign;

public class PullResponse {

    private DateTime currentDt;
    private int orientation;
    private boolean clearCache;
    private boolean restart;
    private LocalTime workStartAt;
    private LocalTime workEndAt;
    private DateTime lastUpdate;
    private int audioOut;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private List<Campaign> campaigns = new ArrayList<>();
    private boolean kioskMode;
    private boolean videoWall;
    private boolean openApp;
    private List<String> days;
    private List<String> displays;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String error;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String status;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Integer nextFile;



    public PullResponse() {

    }

    public DateTime getCurrentDt() {
        return currentDt;
    }

    public void setCurrentDt(DateTime currentDt) {
        this.currentDt = currentDt;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public boolean isClearCache() {
        return clearCache;
    }

    public void setClearCache(boolean clearCache) {
        this.clearCache = clearCache;
    }

    public boolean isRestart() {
        return restart;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }

    public LocalTime getWorkStartAt() {
        return workStartAt;
    }

    public void setWorkStartAt(LocalTime workStartAt) {
        this.workStartAt = workStartAt;
    }

    public DateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(DateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getAudioOut() {
        return audioOut;
    }

    public void setAudioOut(int audioOut) {
        this.audioOut = audioOut;
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public boolean isKioskMode() {
        return kioskMode;
    }

    public void setKioskMode(boolean kioskMode) {
        this.kioskMode = kioskMode;
    }

    public boolean isVideoWall() {
        return videoWall;
    }

    public void setVideoWall(boolean videoWall) {
        this.videoWall = videoWall;
    }

    public boolean isOpenApp() {
        return openApp;
    }

    public void setOpenApp(boolean openApp) {
        this.openApp = openApp;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    public List<String> getDisplays() {
        return displays;
    }

    public void setDisplays(List<String> displays) {
        this.displays = displays;
    }

    public LocalTime getWorkEndAt() {
        return workEndAt;
    }

    public void setWorkEndAt(LocalTime workEndAt) {
        this.workEndAt = workEndAt;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public Integer getNextFile() {
        return nextFile;
    }

    public void setNextFile(Integer nextFile) {
        this.nextFile = nextFile;
    }
}
