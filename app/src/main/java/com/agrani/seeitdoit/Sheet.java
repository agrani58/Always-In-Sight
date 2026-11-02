package com.agrani.seeitdoit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Shared helpers: where the handwriting is stored and how a sheet is drawn. */
final class Sheet {

    /** Logical size of one sheet in pixels (square). */
    static final int SIZE = 800;
    /** Number of big ruled rows per sheet. */
    static final int ROWS = 8;

    static final String TODAY = "today";
    static final String WEEK = "week";

    private Sheet() {}

    static File file(Context c, String name) {
        return new File(c.getFilesDir(), name + ".png");
    }

    /** Draws the ruled rows. Grey so it shows on light and dark wallpapers. */
    static void drawRules(Canvas canvas) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0x88777777);
        p.setStrokeWidth(2f);
        float row = SIZE / (float) ROWS;
        for (int i = 1; i <= ROWS; i++) {
            float y = i * row - 1f;
            canvas.drawLine(16f, y, SIZE - 16f, y, p);
        }
    }

    /** Loads the saved handwriting, or null if nothing was written yet. */
    static Bitmap loadInk(Context c, String name) {
        File f = file(c, name);
        if (!f.exists()) return null;
        return BitmapFactory.decodeFile(f.getAbsolutePath());
    }

    /** Builds the picture the widget shows: ruled rows plus handwriting. */
    static Bitmap render(Context c, String name) {
        Bitmap out = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        drawRules(canvas);
        Bitmap ink = loadInk(c, name);
        if (ink != null) {
            canvas.drawBitmap(ink, 0f, 0f, null);
            ink.recycle();
        }
        return out;
    }

    /** Saves handwriting to private app storage (disk, not memory). */
    static void saveInk(Context c, String name, Bitmap ink) throws IOException {
        File tmp = new File(c.getFilesDir(), name + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            ink.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        File dest = file(c, name);
        if (!tmp.renameTo(dest)) {
            throw new IOException("Could not save " + name);
        }
    }
}
