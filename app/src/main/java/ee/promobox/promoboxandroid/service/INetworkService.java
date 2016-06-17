package ee.promobox.promoboxandroid.service;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.List;

import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.CampaignFile;


public interface INetworkService {

    ListenableFuture<PullResponse> pullRequest(PullRequest pullRequest);

    ListenableFuture<List<File>> downloadCampaigns(final List<Campaign> campaigns);

    ListenableFuture<File> downloadFile(Campaign campaign, CampaignFile campaignFile);
}
