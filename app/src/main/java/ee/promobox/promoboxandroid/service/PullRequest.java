package ee.promobox.promoboxandroid.service;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PullRequest {

    private boolean isOnTop;
    private String onTopActivity;
    private List<String> ip;
    private long freeSpace;
    private boolean force;
    private long cache;
    private int currentFileId;
    private int currentCampaignId;
    private int version;
    @JsonProperty("loadingCampaingId")
    private int loadingCampaignId;
    @JsonProperty("loadingCampaingProgress")
    private int loadingCampaignProgress;
    private String uuid;


    public boolean isOnTop() {
        return isOnTop;
    }

    public void setIsOnTop(boolean isOnTop) {
        this.isOnTop = isOnTop;
    }

    public String getOnTopActivity() {
        return onTopActivity;
    }

    public void setOnTopActivity(String onTopActivity) {
        this.onTopActivity = onTopActivity;
    }

    public List<String> getIp() {
        return ip;
    }

    public void setIp(List<String> ip) {
        this.ip = ip;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public long getCache() {
        return cache;
    }

    public void setCache(long cache) {
        this.cache = cache;
    }

    public int getCurrentFileId() {
        return currentFileId;
    }

    public void setCurrentFileId(int currentFileId) {
        this.currentFileId = currentFileId;
    }

    public int getCurrentCampaignId() {
        return currentCampaignId;
    }

    public void setCurrentCampaignId(int currentCampaignId) {
        this.currentCampaignId = currentCampaignId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getLoadingCampaignId() {
        return loadingCampaignId;
    }

    public void setLoadingCampaignId(int loadingCampaignId) {
        this.loadingCampaignId = loadingCampaignId;
    }

    public int getLoadingCampaignProgress() {
        return loadingCampaignProgress;
    }

    public void setLoadingCampaignProgress(int loadingCampaignProgress) {
        this.loadingCampaignProgress = loadingCampaignProgress;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
