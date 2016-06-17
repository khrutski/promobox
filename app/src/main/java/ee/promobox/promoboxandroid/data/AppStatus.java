package ee.promobox.promoboxandroid.data;


import android.os.Parcel;
import android.os.Parcelable;

public enum AppStatus implements Parcelable{
    PLAYING ,
    DOWNLOADING,
    NO_FILES,
    NO_ACTIVE_CAMPAIGN,
    DEVICE_NOT_ACTIVE,
    CLOSED;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());

    }

    public static final Creator<AppStatus> CREATOR = new Creator<AppStatus>() {
        @Override
        public AppStatus createFromParcel(final Parcel source) {
            return AppStatus.valueOf(source.readString());
        }

        @Override
        public AppStatus[] newArray(final int size) {
            return new AppStatus[size];
        }
    };
}
