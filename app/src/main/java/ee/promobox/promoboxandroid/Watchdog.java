package ee.promobox.promoboxandroid;


public class Watchdog {

    private final static long MAX_DELAY = 5000;
    private long lastTimeUpdate = System.currentTimeMillis();

    public void update() {
        this.lastTimeUpdate = System.currentTimeMillis();
    }

    public boolean isOK() {
        long delay = System.currentTimeMillis() - this.lastTimeUpdate;
        return delay < MAX_DELAY;
    }

}
