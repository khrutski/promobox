package ee.promobox.promoboxandroid.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.NetworkOnMainThreadException;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class InternetConnectionUtil {


    private static final String TAG = "InternetConnectionUtil";

    public static boolean isNetworkConnected(Context context) {

        boolean result = true;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return result;
        }

        NetworkInfo ni = manager.getActiveNetworkInfo();

        if (ni == null) {
            result = false;
        }

        try {
            InetAddress ipAddress = InetAddress.getByName("www.google.com");

            if (ipAddress == null || ipAddress.toString().equals("")) {
                result = false;
            }

        } catch (UnknownHostException e) {
            result = false;
        } catch (NetworkOnMainThreadException e) {
        }
        return result;
    }

}
