package ee.promobox.promoboxandroid.data;


import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PlayList implements IPlayList {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayList.class);

    private List<PlayListItem> playList = Lists.newArrayList();
    private int position = 0;


    public  PlayList() {
    }


    public PlayList(List<Campaign> campaigns) {
        Preconditions.checkNotNull(campaigns);

        for (Campaign campaign: campaigns) {
            for (CampaignFile campaignFile: campaign.getFiles()) {
                playList.add(new PlayListItem(campaign, campaignFile));
            }
        }

        this.position = 0;
    }

    @Override
    public void play(PlayListItem playListItem) {
        Preconditions.checkArgument(playList.contains(playListItem));

        position = playList.indexOf(playListItem);

    }

    @Override
    public void seek(PlayListItem playListItem) {
        Preconditions.checkArgument(playList.contains(playListItem));
        position = playList.indexOf(playListItem);
    }

    @Override
    public Optional<PlayListItem> play() {
        if (playList.size() == 0) {
            return Optional.absent();
        }

        position++;

        if (position >= playList.size()) {
            position = 0;
        }

        LOGGER.debug("Play item: " + current().toString());

        return Optional.of(playList.get(position));
    }

    @Override
    public PlayListItem next() {
        int newPosition = position + 1;

        if (newPosition >= playList.size()) {
            newPosition = 0;
        }

        return playList.get(newPosition);
    }

    @Override
    public PlayListItem previews() {
        int newPosition = position - 1;

        if (newPosition < 0) {
            newPosition = playList.size() - 1;
        }

        return playList.get(newPosition);
    }

    @Override
    public PlayListItem current() {
        return playList.get(position);
    }


    @Override
    public Campaign currentCampaign() {
        return current().getCampaign();
    }

    @Override
    public Optional<PlayListItem> findPlayListItemByFileId(int campaignFileID) {
        for (PlayListItem cFile: playList) {
            if (cFile.getCampaignFile().getId() == campaignFileID) {
                return Optional.of(cFile);
            }
        }

        return Optional.absent();
    }

    @Override
    public boolean isEmpty() {
        return playList.isEmpty();
    }

}
