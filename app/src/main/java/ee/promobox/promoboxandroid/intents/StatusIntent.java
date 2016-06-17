package ee.promobox.promoboxandroid.intents;

import android.content.Intent;
import android.os.Parcelable;

import ee.promobox.promoboxandroid.MainActivity;
import ee.promobox.promoboxandroid.data.AppStatus;


public class StatusIntent extends Intent {
    public StatusIntent(AppStatus appStatus, String status){
        super(MainActivity.SET_STATUS);

        super.putExtra("status", status);
        super.putExtra("statusEnum", (Parcelable)appStatus);
    }
}
