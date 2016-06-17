package ee.promobox.promoboxandroid.util;

import android.support.annotation.Nullable;

import com.google.common.base.Function;


public class DayOfWeekConverter implements Function<String, Integer> {
    @Nullable
    @Override
    public Integer apply(String input) {
        int day = 1;

        switch (input) {
            case "mo":
                day = 1;
                break;
            case "tu":
                day = 2;
                break;
            case "we":
                day = 3;
                break;
            case "th":
                day = 4;
                break;
            case "fr":
                day = 5;
                break;
            case "sa":
                day = 6;
                break;
            case "su":
                day = 7;
                break;
        }

        return day;
    }
}
