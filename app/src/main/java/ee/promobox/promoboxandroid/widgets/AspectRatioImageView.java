package ee.promobox.promoboxandroid.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AspectRatioImageView extends ImageView {
    private static final Logger LOGGER = LoggerFactory.getLogger(AspectRatioImageView.class);

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (widthMeasureSpec == 0 || heightMeasureSpec == 0
                || MeasureSpec.getSize(widthMeasureSpec) == 0 || MeasureSpec.getSize(heightMeasureSpec) == 0) {
            //LOGGER.debug("AspectRatioImageView", "(widthMeasureSpec == 0 || heightMeasureSpec == 0");
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        try {
            //LOGGER.debug("onMeasure");
            Drawable drawable = getDrawable();

            if (drawable == null) {
                setMeasuredDimension(0, 0);
            } else {
                float imageSideRatio = (float) drawable.getIntrinsicWidth() / (float) drawable.getIntrinsicHeight();
                float viewSideRatio = (float) MeasureSpec.getSize(widthMeasureSpec) / (float) MeasureSpec.getSize(heightMeasureSpec);
                int width;
                int height;
                if (imageSideRatio <= viewSideRatio) {
                    // Image is taller than the display (ratio)
                    if (viewSideRatio > 1) { // Wide monitor
                        if (imageSideRatio > 1) { // Wide picture
//                            Log.w(TAG, "*** 1 ***");
                            width = MeasureSpec.getSize(widthMeasureSpec);
                            height = (int) (width / imageSideRatio);
                            setMeasuredDimension(width, height);
                        } else { // Tall picture
//                            Log.w(TAG, "*** 2 ***");
                            height = MeasureSpec.getSize(heightMeasureSpec);
                            width = (int) (height * imageSideRatio);
                            setMeasuredDimension(width, height);
                        }
                    } else { // Tall monitor
                        if (imageSideRatio > 1) { // Wide picture
//                            Log.w(TAG, "*** 3 ***");
                            width = MeasureSpec.getSize(widthMeasureSpec);
                            height = (int) (width / imageSideRatio);
                            setMeasuredDimension(width, height);
                        } else { // Tall picture
//                            Log.w(TAG, "*** 4 ***");
                            width = MeasureSpec.getSize(widthMeasureSpec);
                            height = (int) (width / imageSideRatio);
                            setMeasuredDimension(width, height);
                        }
                    }
                } else {
                    // Image is wider than the display (ratio)
                    if (viewSideRatio > 1) { // Wide monitor
                        if (imageSideRatio > 1) { // Wide picture
//                            Log.w(TAG, "*** 5 ***");
                            height = MeasureSpec.getSize(heightMeasureSpec);
                            width = (int) (height * imageSideRatio);
                            setMeasuredDimension(width, height);
                        } else { // Tall picture
//                            Log.w(TAG, "*** 6 ***");
                            height = MeasureSpec.getSize(heightMeasureSpec);
                            width = (int) (height * imageSideRatio);
                            setMeasuredDimension(width, height);
                        }
                    } else { // Tall monitor
                        if (imageSideRatio > 1) { // Wide picture
//                            Log.w(TAG, "*** 7 ***");
                            width = MeasureSpec.getSize(widthMeasureSpec);
                            height = (int) (width / imageSideRatio);
                            setMeasuredDimension(width, height);
                        } else { // Tall picture
//                            Log.w(TAG, "*** 8 ***");
                            height = MeasureSpec.getSize(heightMeasureSpec);
                            width = (int) (height * imageSideRatio);
                            setMeasuredDimension(width, height);
                        }
                    }
                }
            }
        } catch (Exception e) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }


}
