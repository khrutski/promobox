package ee.promobox.promoboxandroid.util;

public class SettingsSavingException extends Exception {
    private static final String MESSAGE = "Could not save settings because of %s";

    public SettingsSavingException (Exception e){
        super(String.format(MESSAGE, e.getMessage()));
    }
}
