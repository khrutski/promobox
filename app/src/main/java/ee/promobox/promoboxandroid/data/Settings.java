package ee.promobox.promoboxandroid.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;


public class Settings implements Parcelable{

    public static final String SYNC_FREQUENCY_PREF = "sync_frequency";
    public static final String UUID_PREF = "uuid";
    public static final String SERVER = "server";

    public static final Parcelable.Creator<Settings> CREATOR = new Parcelable.Creator<Settings>() {
        public Settings createFromParcel(Parcel in) {
            return new Settings(in);
        }

        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };
    private int syncFrequency = 30;
    private String uuid = "fail";
    private String server = "https://api.promobox.ee/";

    public Settings(){

    }

    public Settings(Parcel in) {
        syncFrequency = in.readInt();
        uuid = in.readString();
        setServer(in.readString());
    }

    public Settings(int syncFrequency, String uuid, String server) {
        this.syncFrequency = syncFrequency;
        this.uuid = uuid;
        this.setServer(server);
    }

    public static Settings copy(Settings settings) {
        return new Settings(settings.syncFrequency, settings.getUuid(), settings.getServer());
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(SYNC_FREQUENCY_PREF,syncFrequency);
        json.put(UUID_PREF,uuid);
        json.put(SERVER, getServer());

        return json;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getSyncFrequency() {
        return syncFrequency;
    }

    public void setSyncFrequency(int syncFrequency) {
        this.syncFrequency = syncFrequency;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dst, int flags) {
        dst.writeInt(syncFrequency);
        dst.writeString(uuid);
        dst.writeString(getServer());
    }

    @Override
    public boolean equals(Object o) {
        if (Settings.class != o.getClass()) return false;

        Settings settings = (Settings) o;

        boolean equals = settings.syncFrequency == syncFrequency;
        equals &= settings.uuid.equals(uuid);
        return equals;
    }

    @Override
    public String toString() {
        return "uuid=" + uuid;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
