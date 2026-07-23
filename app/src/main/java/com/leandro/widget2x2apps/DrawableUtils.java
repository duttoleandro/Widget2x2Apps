package com.leandro.widget2x2apps;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public final class DrawableUtils {
    private DrawableUtils() {}
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int w = Math.max(1, drawable.getIntrinsicWidth());
        int h = Math.max(1, drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
