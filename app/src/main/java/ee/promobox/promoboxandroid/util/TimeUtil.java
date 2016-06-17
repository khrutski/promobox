package ee.promobox.promoboxandroid.util;

import android.support.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ee.promobox.promoboxandroid.data.AppState;
import ee.promobox.promoboxandroid.data.Campaign;


public class TimeUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeUtil.class);

    public static String getTimeString ( long timeMillis) {
        String hms;
        if ( TimeUnit.MILLISECONDS.toHours(timeMillis) > 1 ) {
            hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeMillis),
                    TimeUnit.MILLISECONDS.toMinutes(timeMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeMillis)),
                    TimeUnit.MILLISECONDS.toSeconds(timeMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMillis)));
        } else {
            hms = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(timeMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeMillis)),
                    TimeUnit.MILLISECONDS.toSeconds(timeMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMillis)));
        }
        return hms;
    }

    public static boolean isIncorrectTimePeriod(DateTime lastTimeUpdate) {
        long timeFromLastUpdate = System.currentTimeMillis() - lastTimeUpdate.getMillis();

        return timeFromLastUpdate < 0 || timeFromLastUpdate > 60000;
    }

    public static boolean checkDeviceActive(final AppState appState) {

        int day = DateTime.now().dayOfWeek().get();

        boolean dayOK = appState.getDeviceWorkDays().contains(day);

        LocalTime time = LocalTime.now();

        boolean timeOK = time.isAfter(appState.getWorkHourFrom()) && time.isBefore(appState.getWorkHourTo().minusMinutes(1));

        LOGGER.info("Device is active = {}", dayOK && timeOK);

        return dayOK && timeOK;
    }


    public static boolean hasToBePlayed(Campaign campaign) {

        DateTime currentDate = DateTime.now();

        if (currentDate.getYear() < 2015) {
            return true;
        }

        if(currentDate.isAfter(campaign.getStartDate()) && currentDate.isBefore(campaign.getEndDate())) {

            Integer dayOfWeek = currentDate.getDayOfWeek();
            Integer hourOfDay = currentDate.getHourOfDay();

            List<Integer> days = Lists.transform(campaign.getDays(), new DayOfWeekConverter());
            List<Integer> hours = Lists.transform(campaign.getHours(), new Function<String, Integer>() {
                @Nullable
                @Override
                public Integer apply(String input) {
                    return Integer.parseInt(input);
                }
            });

            if(days.contains(dayOfWeek) && hours.contains(hourOfDay)){
                return true;
            }
        }
        return false;
    }
}
