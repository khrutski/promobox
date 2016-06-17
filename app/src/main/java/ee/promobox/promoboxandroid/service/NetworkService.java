package ee.promobox.promoboxandroid.service;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import ee.promobox.promoboxandroid.data.AppState;
import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignFile;
import ee.promobox.promoboxandroid.util.AppStateHelper;


public class NetworkService implements INetworkService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkService.class);

    private ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(5));
    private ListeningExecutorService executorDownloadService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(5));

    private AppState appState;


    public NetworkService(AppState appState) {
        this.appState = appState;
    }


    @Override
    public ListenableFuture<PullResponse> pullRequest(final PullRequest pullRequest) {
        return getExecutorService().submit(new HttpPullRequest(appState, pullRequest));

    }



    @Override
    public ListenableFuture<List<File>> downloadCampaigns(final List<Campaign> campaigns) {

        List<ListenableFuture<File>> listDownloads = new ArrayList<>();

        for (Campaign campaign : campaigns) {
            for (CampaignFile file : campaign.getFiles()) {

                CampaignFile oldCampaignFile = AppStateHelper.findCampaignFile(file.getId(), appState.getCampaigns());

                if (file.getType().isDownloadable()) {
                    if (oldCampaignFile == null
                            || file.getUpdatedDt() != oldCampaignFile.getUpdatedDt()
                            || !FileService.getFile(campaign, file).exists()) {

                        LOGGER.debug("Downloading file: {}", file.toString());
                        listDownloads.add(downloadFile(campaign, file));
                    }
                }

            }
        }

        if (!listDownloads.isEmpty()) {
            return Futures.allAsList(listDownloads);
        }

        return Futures.immediateFuture((List<File>) new ArrayList<File>());
    }



    @Override
    public ListenableFuture<File> downloadFile(final Campaign campaign, final CampaignFile campaignFile) {

        return  executorDownloadService.submit(new Callable<File>() {
            @Override
            public File call() throws Exception {
                appState.setLoadingCampaign(campaign);
                appState.setLoadingCampaignProgress(AppStateHelper.calculateDownloadProgress(campaign, campaignFile));

                URL url = new URL(appState.getSettings().getServer() + "/service/files/" + campaignFile.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                try {

                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.setDoOutput(true);

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                        File tempFile = FileService.getTempFile(campaign, campaignFile);
                        File file = FileService.getFile(campaign, campaignFile);

                        try (InputStream httpInputStream = new BufferedInputStream(conn.getInputStream());
                             FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile)) {

                            IOUtils.copy(httpInputStream, tempFileOutputStream);

                            IOUtils.closeQuietly(httpInputStream);
                            IOUtils.closeQuietly(tempFileOutputStream);

                            if (tempFile.exists()) {
                                if (tempFile.renameTo(file)) {
                                    LOGGER.debug("Size " + file.getAbsolutePath() + " = " + file.length());
                                } else {
                                    LOGGER.error("Can't rename tmp file: {}", tempFile.getAbsolutePath());
                                }
                            }

                        }

                        return file;
                    }

                } catch (Exception ex) {
                    LOGGER.error(ex.toString(), ex);
                } finally {
                    conn.disconnect();
                }

                throw new Error("Error get response from server");
            }
        });
    }

    public ListeningExecutorService getExecutorService() {
        return executorService;
    }

}
