package ee.promobox.promoboxandroid.util;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import ee.promobox.promoboxandroid.MainService;
import ee.promobox.promoboxandroid.data.Settings;


public class SettingsAdapter {

    private static final String SETTINGS_FILE_NAME = "settings";

    Settings settings = new Settings();

    private File settingsFile = null;

    public SettingsAdapter(File ROOT){
        settingsFile = new File(ROOT, SETTINGS_FILE_NAME);

        JSONObject json;
        json = readJSONFromFile(settingsFile);

        if (json != null){
            handleJSON(json);
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) throws SettingsSavingException {
        try {
            writeToFile(settings);
            this.settings = settings;
        } catch (IOException | JSONException e) {
            throw new SettingsSavingException(e);
        }
    }

    private JSONObject readJSONFromFile(File file) {
        if (!file.exists()) return null;

        JSONObject json;
        try {
            json = new JSONObject(FileUtils.readFileToString(file));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            json = null;
        }
        return json;
    }

    private void handleJSON(JSONObject json) {
        if (json == null) return;
        try {
            if (json.has(Settings.SYNC_FREQUENCY_PREF)){
                settings.setSyncFrequency(json.getInt(Settings.SYNC_FREQUENCY_PREF));
            }
            if (json.has(Settings.UUID_PREF)){
                settings.setUuid(json.getString(Settings.UUID_PREF));
            }
            if (json.has(Settings.SERVER)){
                settings.setServer(json.getString(Settings.SERVER));
            } else {
                settings.setServer(MainService.DEFAULT_SERVER);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public String getUuid() {
        return settings.getUuid();
    }

    public void setUuid(String uuid) throws SettingsSavingException {
        Settings copy = Settings.copy(settings);
        copy.setUuid(uuid);
        try {
            writeToFile(copy);
            settings = copy;
        } catch (IOException | JSONException e) {
            throw new SettingsSavingException(e);
        }
    }

    public void setValues(int syncFrequency, String uuid, String server) throws SettingsSavingException {
        Settings newSettings = new Settings(syncFrequency, uuid, server);
        try {
            writeToFile(newSettings);
            settings = newSettings;
        } catch (IOException | JSONException e) {
            throw new SettingsSavingException(e);
        }
    }

    private void writeToFile(Settings settings) throws IOException, JSONException {
        FileUtils.writeStringToFile(settingsFile, settings.getJSON().toString());
    }
}
