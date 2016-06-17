package ee.promobox.promoboxandroid.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by khrutski on 16.6.16.
 */
public class RSSLineView extends View {

    Paint mTextPaint;

    public RSSLineView(Context context, AttributeSet attrs) {

        super(context, attrs);
        init();
    }

    private void init() {

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(120);
    }

    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        canvas.drawText("test text", 100, 100, mTextPaint);
    }
}
