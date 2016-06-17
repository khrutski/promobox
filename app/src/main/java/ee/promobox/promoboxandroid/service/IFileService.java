package ee.promobox.promoboxandroid.service;

import java.util.List;

import ee.promobox.promoboxandroid.data.AppState;
import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.Settings;
import ee.promobox.promoboxandroid.util.SettingsSavingException;


public interface IFileService {

    void clearCache(List<Campaign> currentCampaigns);

    Settings getSettings();

    void setSettings(Settings settings) throws SettingsSavingException;

    AppState restoreAppState();

    void saveAppState(AppState appState);
}
