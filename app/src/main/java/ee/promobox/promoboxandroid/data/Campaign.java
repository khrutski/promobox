package ee.promobox.promoboxandroid.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;


public class Campaign implements Parcelable, Comparable<Campaign> {

    public final static int ORDER_ASC = 1;
    public final static int ORDER_RANDOM = 2;
    public static final Parcelable.Creator<Campaign> CREATOR = new Parcelable.Creator<Campaign>() {
        public Campaign createFromParcel(Parcel in) {
            return new Campaign(in);
        }

        public Campaign[] newArray(int size) {
            return new Campaign[size];
        }
    };
    protected int position;
    private int clientId;
    private int campaignId;
    private String campaignName;
    private DateTime updateDate;
    private DateTime startDate;
    private DateTime endDate;
    @JsonProperty("sequence")
    private int order;
    private int campaignStatus;
    private int duration;
    private List<String> days = new ArrayList<>();
    private List<String> hours = new ArrayList<>();
    private List<CampaignFile> files = new ArrayList<>();


    public Campaign() {
    }


    public Campaign(Parcel in) {
        clientId = in.readInt();
        campaignId = in.readInt();
        setCampaignName(in.readString());
        updateDate = new DateTime(in.readLong());
        startDate = new DateTime(in.readLong());
        endDate = new DateTime(in.readLong());
        setOrder(in.readInt());
        position = in.readInt();
        campaignStatus = in.readInt();
        duration = in.readInt();
        in.readStringList(days);
        in.readStringList(hours);
        in.readTypedList(files, CampaignFile.CREATOR);
    }

    public int getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public List<CampaignFile> getFiles() {
        return files;
    }

    public void setFiles(List<CampaignFile> files) {
        this.files = files;
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(DateTime updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(clientId);
        dest.writeInt(campaignId);
        dest.writeString(getCampaignName());
        dest.writeLong(updateDate.toDate().getTime());
        dest.writeLong(startDate.toDate().getTime());
        dest.writeLong(endDate.toDate().getTime());
        dest.writeInt(getOrder());
        dest.writeInt(position);
        dest.writeInt(campaignStatus);
        dest.writeInt(duration);
        dest.writeStringList(days);
        dest.writeStringList(hours);
        dest.writeTypedList(files);
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public List<String> getDays() {
        return days;
    }

    public void setDays(ArrayList<String> days) {
        this.days = days;
    }

    public List<String> getHours() {
        return hours;
    }

    public void setHours(ArrayList<String> hours) {
        this.hours = hours;
    }

    public int getCampaignStatus() {
        return campaignStatus;
    }

    public void setCampaignStatus(int campaignStatus) {
        this.campaignStatus = campaignStatus;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof Campaign) {
            Campaign camp = (Campaign)object;

            return Objects.equal(camp.getCampaignId(), getCampaignId()) &&
                    Objects.equal(camp.getUpdateDate(), getUpdateDate());
        }

        return false;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getCampaignId())
                .add("name", getCampaignName())
                .add("status", getCampaignStatus())
                .add("files", getFiles().size())
                .add("clientId", getClientId()).toString();

    }

    @Override
    public int compareTo(Campaign campaign) {
        if (this.getCampaignId() > campaign.getCampaignId()) {
            return 1;
        } else if (this.getCampaignId() < campaign.getCampaignId()) {
            return -1;
        }

        return 0;
    }
}
