package ee.promobox.promoboxandroid.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;


public class TurningRelativeLayout extends RelativeLayout {
    private boolean rotationSet = false;
    private Integer widthMeasureSpec = null;
    private Integer heightMeasureSpec = null;


    public TurningRelativeLayout(Context context) {
        super(context);
    }

    public TurningRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TurningRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TurningRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (rotationSet) {
            if (this.widthMeasureSpec == null && this.heightMeasureSpec == null) {
                this.widthMeasureSpec = widthMeasureSpec;
                this.heightMeasureSpec = heightMeasureSpec;
            }
            super.onMeasure(this.heightMeasureSpec, this.widthMeasureSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setRotation(float rotation) {
        rotationSet = true;
        super.setRotation(rotation);
    }

    public boolean isRotationSet() {
        return rotationSet;
    }

    public int getInitialWidth() {
        return MeasureSpec.getSize(widthMeasureSpec);
    }

    public int getInitialHeight() {
        return MeasureSpec.getSize(heightMeasureSpec);
    }
}
