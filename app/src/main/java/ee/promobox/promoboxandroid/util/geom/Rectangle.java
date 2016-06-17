package ee.promobox.promoboxandroid.util.geom;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by ilja on 20.02.2015.
 */
public class Rectangle {
    private Point topLeft, topRight, bottomRight, bottomLeft;

    /**
     * Clockwise points from top left.
     */
    public Rectangle(Point topLeft,Point topRight,Point bottomRight,Point bottomLeft){
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
    }

    public Rectangle(Point[] points){
        this.topLeft = points[0];
        this.topRight = points[1];
        this.bottomRight = points[2];
        this.bottomLeft = points[3];
    }

    public Rect getOuterRect(){
        int leftX = topLeft.x, rightX = topLeft.x, topY = topLeft.y, bottomY = topLeft.y;

        if (leftX > topRight.x) leftX = topRight.x;
        if (leftX > bottomRight.x) leftX = bottomRight.x;
        if (leftX > bottomLeft.x) leftX = bottomLeft.x;

        if (rightX < topRight.x) rightX = topRight.x;
        if (rightX < bottomRight.x) rightX = bottomRight.x;
        if (rightX < bottomLeft.x) rightX = bottomLeft.x;

        if (bottomY > topRight.y) bottomY = topRight.y;
        if (bottomY > bottomRight.y) bottomY = bottomRight.y;
        if (bottomY > bottomLeft.y) bottomY = bottomLeft.y;

        if (topY < topRight.y) topY = topRight.y;
        if (topY < bottomRight.y) topY = bottomRight.y;
        if (topY < bottomLeft.y) topY = bottomLeft.y;


        return new Rect(leftX,topY,rightX,bottomY);
    }

    public static Rect getOuterRect(Point[] points){
        Rectangle rectangle = new Rectangle(points);
        return rectangle.getOuterRect();
    }

    public double getAvgAngle(){
        return TriangleEquilateral.getAngleAlpha(bottomLeft, topLeft);
    }
}
