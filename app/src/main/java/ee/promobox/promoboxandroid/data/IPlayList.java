package ee.promobox.promoboxandroid.data;


import com.google.common.base.Optional;

public interface IPlayList {

    void play(PlayListItem playListItem);
    void seek(PlayListItem playListItem);

    Optional<PlayListItem> play();
    PlayListItem next();
    PlayListItem previews();
    PlayListItem current();

    Campaign currentCampaign();
    Optional<PlayListItem> findPlayListItemByFileId(int campaignFileID);

    boolean isEmpty();
}
