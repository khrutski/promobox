package ee.promobox.promoboxandroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;


public class InitReceiver extends BroadcastReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitReceiver.class);


    private static final int REPEAT_TIME_SECONDS = 60;
    public static final long REPEAT_TIME = 1000 * REPEAT_TIME_SECONDS;

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGGER.debug("MyScheduleReceiver started");

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(MainActivity.UI_RESURRECT) ) {
            LOGGER.info("Device booted");
            Intent mainService = new Intent(context, MainService.class);
            mainService.putExtra("startMainActivity", true);
            context.startService(mainService);
        } else if (action.equals(MainActivity.APP_START) ) {
            LOGGER.info("Application started");
            Intent mainService = new Intent(context, MainService.class);
            context.startService(mainService);
        }

        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Intent mainServiceIntent = new Intent(context, MainService.class);
        mainServiceIntent.putExtra("serviceAlarm", Boolean.TRUE);

        PendingIntent pending = PendingIntent.getService(context, 0, mainServiceIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.cancel(pending);

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.SECOND, REPEAT_TIME_SECONDS);

        LOGGER.debug("Started fetch every {} seconds", REPEAT_TIME_SECONDS);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(), REPEAT_TIME, pending);


    }
}
