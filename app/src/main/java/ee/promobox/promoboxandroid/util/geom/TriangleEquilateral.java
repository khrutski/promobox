package ee.promobox.promoboxandroid.util.geom;

import android.graphics.Point;
import android.util.Log;

/**
 * Created by ilja on 20.02.2015.
 */
public class TriangleEquilateral {

    // clockWise
    private static final double QUARTER_ONE = 0d;
    private static final double QUARTER_TWO = 1d;
    private static final double QUARTER_THREE = 2d;
    private static final double QUARTER_FOUR = 3d;


    private double quarter;

    private Point a,b,c;

    public TriangleEquilateral(Point a, Point b){
        this.a = a;
        this.b = b;
        this.c = getC(a,b);
    }

    /**
     * Returns an angle on clock (0 - center - time) .
     * Vertical line (a,b) where a is bottom is with angle 0.
     */
    public static double getAngleAlpha(Point clockCenter, Point time){
        TriangleEquilateral triangle = new TriangleEquilateral(clockCenter,time);
        return triangle.getAngleAlpha();
    }

    public static boolean isTurned(Point clockCenter, Point time){
        TriangleEquilateral triangle = new TriangleEquilateral(clockCenter,time);
        return triangle.quarter == QUARTER_TWO || triangle.quarter == QUARTER_FOUR;
    }

    public double getAngleAlpha(){
        Log.d("TRIANGLE" , "quarter = " + quarter);
        Log.d("TRIANGLE" , "a = ("+a.x+";"+a.y+") b = ("+b.x+";"+b.y+") c = ("+c.x+";"+c.y+")");
        double lineA = Line.getLineLength(b,c);
        double lineC = Line.getLineLength(a,b);
        Log.d("TRIANGLE" , "lineA length = " + lineA + " lineC length = " + lineC);
        return Math.floor(Math.sin( lineC != 0 ? (lineA / lineC): 0 ) + 90 * quarter);
    }

    private Point getC (Point a, Point b){
        Point c = new Point();
        if (a.x <= b.x) { // a levee
            if (a.y < b.y ) { // i nizhe
                c.x = a.x;
                c.y = b.y;
                quarter = QUARTER_ONE;
            } else { // i vishe
                c.x = b.x;
                c.y = a.y;
                quarter = QUARTER_TWO;
            }
        } else { // a pravee
            if (a.y > b.y ) { // i vishe
                c.x = a.x;
                c.y = b.y;
                quarter = QUARTER_THREE;
            } else {  //i nizhe
                c.x = b.x;
                c.y = a.y;
                quarter = QUARTER_FOUR;
            }
        }
        return c;
    }
}
