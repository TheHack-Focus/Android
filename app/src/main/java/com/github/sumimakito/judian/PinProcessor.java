package com.github.sumimakito.judian;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class PinProcessor {
    public final static int x = 76;
    public final static int y = 16;
    public final static int d = 227;
    public final static int od = 377;
    public final static int fd = 144;
    public final static RectF DSTRECT = new RectF(x, y, x + d, y + d);

    public static Bitmap process(Context context, Bitmap avatar) {
        Bitmap pinSource = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin);
        Bitmap scaledAvatar = Bitmap.createScaledBitmap(avatar, od, od, true);
        Bitmap roundedAvatar = Bitmap.createBitmap(od, od, Bitmap.Config.ARGB_8888);

        Canvas roundedCanvas = new Canvas(roundedAvatar);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(scaledAvatar, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);
        roundedCanvas.drawCircle(od * 0.5f, od * 0.5f, od * 0.5f, paint);

        Bitmap mutableBitmap = pinSource.copy(Bitmap.Config.ARGB_8888, true);
        Canvas mergedCanvas = new Canvas(mutableBitmap);
        mergedCanvas.drawBitmap(roundedAvatar, null, DSTRECT, new Paint());
        Bitmap finalBitmap = Bitmap.createScaledBitmap(mutableBitmap, fd, fd, true);
        pinSource.recycle();
        scaledAvatar.recycle();
        roundedAvatar.recycle();
        mutableBitmap.recycle();
        return finalBitmap;
    }
}
