// AIDLInterface.aidl
package ee.promobox.promoboxandroid;

import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.Settings;
import ee.promobox.promoboxandroid.data.AppStatus;

interface AIDLInterface {
     Campaign getCampaignWithId(int campaignId);
     boolean isDeviceActive();
     boolean isKioskMode();
     void setCurrentFileId(int id);
     void setCurrentCampaignId(int campaignId);
     int getCurrentFileId();
     void setUuid(String uuid);
     String getUuid();
     int getOrientation();
     List<Campaign> getCurrentCampaigns();
     void setClosedNormally(boolean closedNormally);
     Settings getSettings();
     void setSettings(in Settings settings);
     void updateWatchdog();
     AppStatus getAppStatus();

}