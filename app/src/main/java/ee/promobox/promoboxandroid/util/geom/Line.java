package ee.promobox.promoboxandroid.util.geom;

import android.graphics.Point;

/**
 * Created by ilja on 27.03.2015.
 */
public class Line {
    public static double getLineLength(Point a, Point b){
        return Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y , 2));
    }
}
