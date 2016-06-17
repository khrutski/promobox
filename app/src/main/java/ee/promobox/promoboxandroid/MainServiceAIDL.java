package ee.promobox.promoboxandroid;

import android.os.RemoteException;

import java.util.List;

import ee.promobox.promoboxandroid.data.AppStatus;
import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.Settings;
import ee.promobox.promoboxandroid.util.TimeUtil;


public class MainServiceAIDL extends AIDLInterface.Stub {
    private MainService mainService;

    public MainServiceAIDL(MainService mainService){
        this.mainService = mainService;
    }

    @Override
    public Campaign getCampaignWithId(int campaignId) throws RemoteException {
        if (mainService.getAppState().getCampaigns() != null){
            for (Campaign campaign : mainService.getAppState().getCampaigns()){
                if (campaign.getCampaignId() == campaignId){
                    return campaign;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isDeviceActive() throws RemoteException {
        return TimeUtil.checkDeviceActive(mainService.getAppState());
    }

    public boolean isKioskMode(){
        return mainService.getAppState().isKioskMode();
    }

    @Override
    public void setCurrentCampaignId(int campaignId) throws RemoteException {
        mainService.getAppState().setCurrentCampaignId(campaignId);
    }

    @Override
    public int getCurrentFileId() throws RemoteException {
        return mainService.getAppState().getCurrentFileId();
    }

    @Override
    public void setCurrentFileId(int id) throws RemoteException {
        mainService.getAppState().setCurrentFileId(id);
    }

    @Override
    public String getUuid() throws RemoteException {
        return mainService.getAppState().getSettings().getUuid();
    }

    @Override
    public void setUuid(String uuid) throws RemoteException {
        mainService.getAppState().getSettings().setUuid(uuid);
        mainService.setSettings(mainService.getAppState().getSettings());
        mainService.checkAndDownloadCampaign();
    }

    @Override
    public int getOrientation() throws RemoteException {
        return mainService.getAppState().getOrientation();
    }

    @Override
    public List<Campaign> getCurrentCampaigns() throws RemoteException {
        return mainService.getAppState().getCurrentCampaigns();
    }

    @Override
    public void setClosedNormally(boolean closedNormally) throws RemoteException {
        mainService.setClosedNormally(closedNormally);
    }

    @Override
    public Settings getSettings() throws RemoteException {
        return mainService.getAppState().getSettings();
    }

    @Override
    public void setSettings(Settings settings) throws RemoteException {
        mainService.getAppState().setSettings(settings);
        mainService.setSettings(mainService.getAppState().getSettings());
        mainService.checkAndDownloadCampaign();
    }

    @Override
    public void updateWatchdog() throws RemoteException {
        mainService.updateWatchdog();
    }

    @Override
    public AppStatus getAppStatus() throws RemoteException {
        return mainService.getAppState().getStatus();
    }

}
