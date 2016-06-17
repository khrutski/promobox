package ee.promobox.promoboxandroid.service;

import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import ee.promobox.promoboxandroid.data.AppState;
import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.data.Settings;
import ee.promobox.promoboxandroid.util.SettingsAdapter;
import ee.promobox.promoboxandroid.util.SettingsSavingException;


public class FileService implements IFileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
    private final static SettingsAdapter settingsAdapter;
    public static File ROOT;

    static {

        if (Environment.getExternalStorageDirectory() != null) {
            ROOT = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/promobox/");
        } else {
            ROOT = new File("/promobox/");
        }

        checkExternalSD();

        settingsAdapter = new SettingsAdapter(ROOT);


    }

    private ObjectMapper mapper = new ObjectMapper();

    public FileService() {
        mapper.registerModule(new JodaModule());
    }

    public static long freeSpace() {
        StatFs stat = new StatFs(FileService.ROOT.getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        return bytesAvailable;
    }

    public static File getFile(final Campaign campaign, final CampaignFile campaignFile) {
        File dir = new File(FileService.ROOT.getAbsolutePath() + String.format("/%s/", campaign.getCampaignId()));

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, String.valueOf(campaignFile.getId()));

        return file;
    }

    public static File getTempFile(final Campaign campaign, final CampaignFile campaignFile) {
        File dir = new File(FileService.ROOT.getAbsolutePath() + String.format("/%s/", campaign.getCampaignId()));

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, String.valueOf(campaignFile.getId()) + ".tmp");

        return file;
    }

    private static void checkExternalSD() {
        File file = new File("/mnt/external_sd");
        if (file.exists() && file.listFiles() != null && file.listFiles().length > 0) {
            LOGGER.debug("/mnt/external_sd EXISTS");
            ROOT = new File(file.getPath() + "/promobox/");
        }
        if (!ROOT.exists()) {
            try {
                FileUtils.forceMkdir(ROOT);
            } catch (IOException ex) {
                LOGGER.debug(ex.getMessage(), ex);
            }
        }
        LOGGER.debug(" ROOT  = " + ROOT.getPath());
    }

    @Override
    public void clearCache(List<Campaign> currentCampaigns) {

        Collection<Integer> ids = Collections2.transform(currentCampaigns, new Function<Campaign, Integer>() {
            @Nullable
            @Override
            public Integer apply(Campaign input) {
                return input.getCampaignId();
            }
        });

        for (File folder : ROOT.listFiles()) {
            if (folder.isDirectory()) {
                try {
                    if (!ids.contains(Integer.parseInt(folder.getName()))) {
                        FileUtils.deleteQuietly(folder);
                    }
                } catch (Exception ex) {
                    LOGGER.info(ex.getMessage(), ex);
                }
            }
        }
    }

    @Override
    public Settings getSettings() {
        return settingsAdapter.getSettings();
    }

    @Override
    public void setSettings(Settings settings) throws SettingsSavingException {
        settingsAdapter.setSettings(settings);
    }

    @Override
    public AppState restoreAppState() {
        File appStateFile = new File(ROOT, "app_state.json");

        AppState state = null;

        try {
            state = mapper.readValue(appStateFile, AppState.class);
        } catch (IOException ex) {
            state = new AppState();
            LOGGER.error(ex.getMessage(), ex);
        }
        state.setSettings(getSettings());

        return state;
    }

    @Override
    public void saveAppState(AppState appState) {
        File appStateFile = new File(ROOT, "app_state.json");
        try {
            Files.write(mapper.writeValueAsString(appState), appStateFile, Charsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }
}
