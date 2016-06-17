package ee.promobox.promoboxandroid.data;


import com.google.common.base.MoreObjects;

import ee.promobox.promoboxandroid.service.FileService;

public class PlayListItem {

    private CampaignFile campaignFile;
    private Campaign campaign;


    public PlayListItem(Campaign campaign, CampaignFile campaignFile) {
        this.setCampaign(campaign);
        this.setCampaignFile(campaignFile);
    }

    public CampaignFile getCampaignFile() {
        return campaignFile;
    }

    public void setCampaignFile(CampaignFile campaignFile) {
        this.campaignFile = campaignFile;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public String getPath() {
        return FileService.ROOT.getPath() + "/" + campaign.getCampaignId() + "/" + campaignFile.getId();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", campaignFile.getId())
                .add("filename", campaignFile.getName())
                .add("orderId", campaignFile.getOrderId())
                .add("size", campaignFile.getSize())
                .add("type", campaignFile.getType().name()).toString();

    }


}
