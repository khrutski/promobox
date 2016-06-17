package ee.promobox.promoboxandroid.widgets;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.IOException;

/**
 * Created by ilja on 18.02.2015.
 */
public class MyAnimatedDrawable extends AnimationDrawable{
    public static final String ZZZ = "zzz/zzz_000%d.png"; // 0 to 49 pictures
    public static final String AUDIO = "audio_animation/Audio_%d.png"; // 0 to 23
    public static final String DOWNLOADING = "downloading/loading files_000%d.png"; // 3 to 51


    private String pattern;

    public MyAnimatedDrawable(Context context, String pattern, int fromInclusive, int toInclusive) {
        this.pattern = pattern;
        setAnimation(context, fromInclusive, toInclusive);
    }

    private void setAnimation(Context context, int from, int to){
        Drawable d;
        try {
            AssetManager assets = context.getAssets();

            for (int i = from; i < to + 1; i++) {
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inDither = false;
                options.inTempStorage = new byte[32 * 1024];
                Bitmap b = BitmapFactory.decodeStream(assets.open(String.format(pattern,i)),null, options);
                d = new BitmapDrawable(Resources.getSystem(),b);
//                d = Drawable.createFromStream(assets.open(String.format(pattern,i)), null);
                this.addFrame(d, 50);
            }

            this.setOneShot(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recycleSelf() {
        stop();
        for (int i = 0; i < getNumberOfFrames(); ++i){
            Drawable frame = getFrame(i);
            if (frame instanceof BitmapDrawable) {
                ((BitmapDrawable)frame).getBitmap().recycle();
            }
            frame.setCallback(null);
        }
        setCallback(null);
    }
}
