package ee.promobox.promoboxandroid.util;


import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import ee.promobox.promoboxandroid.data.AppState;
import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.service.PullResponse;

public class AppStateHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppStateHelper.class);


    private static boolean checkDiffWorkDays(List<Integer> current, List<String> updated) {
        return !current.equals(Lists.transform(updated, new DayOfWeekConverter()));
    }

    public static boolean isAppStateChanged(AppState appState, PullResponse pullResponse) {

        if (appState.getCampaigns().size() != pullResponse.getCampaigns().size()) {
            return true;
        }

        if (appState.isKioskMode() != pullResponse.isKioskMode()) {
            return true;
        }

        if (appState.getOrientation() != pullResponse.getOrientation()) {
            return true;
        }

        if (checkDiffWorkDays(appState.getDeviceWorkDays(), pullResponse.getDays())) {
            return true;
        }


        for (Campaign campaign : pullResponse.getCampaigns()) {
            if (appState.getCampaigns().contains(campaign)) {
                Campaign oldCampaign = appState.getCampaigns().get(appState.getCampaigns().indexOf(campaign));
                if (campaign.getUpdateDate().isAfter(oldCampaign.getUpdateDate())) {
                    return true;
                }
            } else {
                return true;
            }
        }


        return false;
    }

    public static CampaignFile findCampaignFile(int campaignFileId, List<Campaign> campaigns) {
        for (Campaign campaign: campaigns) {
            for (CampaignFile file: campaign.getFiles()) {
                if (file.getId() == campaignFileId) {
                    return file;
                }
            }
        }

        return  null;
    }

    public static List<Campaign> updatedCampaigns(AppState appState, PullResponse pullResponse) {
        List<Campaign> updatedCampaigns = Lists.newArrayList();

        for (Campaign campaign : pullResponse.getCampaigns()) {
            if (appState.getCampaigns().contains(campaign)) {
                Campaign oldCampaign = appState.getCampaigns().get(appState.getCampaigns().indexOf(campaign));
                if (campaign.getUpdateDate().isAfter(oldCampaign.getUpdateDate())) {
                    updatedCampaigns.add(campaign);
                }
            } else {
                updatedCampaigns.add(campaign);
            }
        }

        return updatedCampaigns;
    }

    public static AppState merge(AppState appState, PullResponse pullResponse) {
        Collections.sort(pullResponse.getCampaigns());

        appState.setCampaigns(pullResponse.getCampaigns());

        for (Campaign campaign : appState.getCampaigns()) {
            AppStateHelper.sortCampaignFiles(campaign);
        }

        appState.setDeviceWorkDays(Lists.transform(pullResponse.getDays(), new DayOfWeekConverter()));

        appState.setWorkHourFrom(pullResponse.getWorkStartAt());
        appState.setWorkHourTo(pullResponse.getWorkEndAt());
        appState.setKioskMode(pullResponse.isKioskMode());
        appState.setOrientation(pullResponse.getOrientation());

        return  appState;
    }

    public static void sortCampaignFiles(Campaign campaign) {
        if (campaign.getOrder() == Campaign.ORDER_ASC) {
            Collections.sort(campaign.getFiles());
        } else {
            Collections.shuffle(campaign.getFiles());
        }
    }

    public static int calculateDownloadProgress(Campaign campaign, CampaignFile campaignFile) {
        int downloadProgress = (int) (100f - (float) campaign.getFiles().indexOf(campaignFile) / campaign.getFiles().size() * 100f);

        LOGGER.info("Download progress: {}, {}, {}",
                downloadProgress, campaign, campaignFile);

        return downloadProgress;
    }

}
