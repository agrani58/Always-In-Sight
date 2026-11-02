package com.agrani.seeitdoit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/** One writable sheet. Draws with a stylus (or a finger until a stylus is seen). */
public class DrawView extends View {

    private final Bitmap ink = Bitmap.createBitmap(Sheet.SIZE, Sheet.SIZE, Bitmap.Config.ARGB_8888);
    private final Canvas inkCanvas = new Canvas(ink);
    private final Bitmap undoBmp = Bitmap.createBitmap(Sheet.SIZE, Sheet.SIZE, Bitmap.Config.ARGB_8888);
    private final Canvas undoCanvas = new Canvas(undoBmp);

    private final Paint pen = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eraser = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint panel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF sheetRect = new RectF(0f, 0f, Sheet.SIZE, Sheet.SIZE);

    private final Matrix toView = new Matrix();
    private final Matrix toSheet = new Matrix();
    private final float[] pt = new float[2];
    private final Path path = new Path();

    private float lastX;
    private float lastY;
    private boolean canUndo;
    private boolean dirty;
    private boolean eraserMode;
    private boolean stylusSeen;
    private boolean active;
    private Runnable touchListener;

    public DrawView(Context context) {
        super(context);

        pen.setColor(0xFF111827);
        pen.setStyle(Paint.Style.STROKE);
        pen.setStrokeCap(Paint.Cap.ROUND);
        pen.setStrokeJoin(Paint.Join.ROUND);
        pen.setStrokeWidth(5f);

        eraser.setStyle(Paint.Style.STROKE);
        eraser.setStrokeCap(Paint.Cap.ROUND);
        eraser.setStrokeJoin(Paint.Join.ROUND);
        eraser.setStrokeWidth(40f);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        panel.setColor(0xEEFFFFFF);
        panel.setStyle(Paint.Style.FILL);

        border.setColor(0xFF2563EB);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(8f);
    }

    // ---- public controls -------------------------------------------------

    public void setOnTouchedListener(Runnable r) {
        touchListener = r;
    }

    public void setActive(boolean a) {
        active = a;
        invalidate();
    }

    public void setEraser(boolean on) {
        eraserMode = on;
    }

    public void load(Bitmap saved) {
        if (saved == null) return;
        inkCanvas.drawBitmap(saved, 0f, 0f, null);
        saved.recycle();
        invalidate();
    }

    public Bitmap getInk() {
        return ink;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markSaved() {
        dirty = false;
    }

    public void undo() {
        if (!canUndo) return;
        inkCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        inkCanvas.drawBitmap(undoBmp, 0f, 0f, null);
        canUndo = false;
        dirty = true;
        invalidate();
    }

    public void clearAll() {
        rememberForUndo();
        inkCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        dirty = true;
        invalidate();
    }

    // ---- drawing ---------------------------------------------------------

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float scale = Math.min(w, h) / (float) Sheet.SIZE;
        float dx = (w - Sheet.SIZE * scale) / 2f;
        float dy = (h - Sheet.SIZE * scale) / 2f;
        toView.reset();
        toView.postScale(scale, scale);
        toView.postTranslate(dx, dy);
        toView.invert(toSheet);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.concat(toView);
        canvas.drawRoundRect(sheetRect, 24f, 24f, panel);
        Sheet.drawRules(canvas);
        canvas.drawBitmap(ink, 0f, 0f, null);
        if (active) {
            canvas.drawRoundRect(sheetRect, 24f, 24f, border);
        }
        canvas.restore();
    }

    private void rememberForUndo() {
        undoCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        undoCanvas.drawBitmap(ink, 0f, 0f, null);
        canUndo = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int tool = e.getToolType(0);
        boolean stylus = tool == MotionEvent.TOOL_TYPE_STYLUS || tool == MotionEvent.TOOL_TYPE_ERASER;
        if (stylus) {
            stylusSeen = true;
        } else if (stylusSeen) {
            return true; // palm rejection: ignore fingers once a pen has been used
        }

        Paint paint = (eraserMode || tool == MotionEvent.TOOL_TYPE_ERASER) ? eraser : pen;

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (touchListener != null) touchListener.run();
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                rememberForUndo();
                pt[0] = e.getX();
                pt[1] = e.getY();
                toSheet.mapPoints(pt);
                lastX = pt[0];
                lastY = pt[1];
                path.reset();
                path.moveTo(lastX, lastY);
                inkCanvas.drawPoint(lastX, lastY, paint);
                dirty = true;
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < e.getHistorySize(); i++) {
                    segment(e.getHistoricalX(i), e.getHistoricalY(i), paint);
                }
                segment(e.getX(), e.getY(), paint);
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                path.lineTo(lastX, lastY);
                inkCanvas.drawPath(path, paint);
                path.reset();
                invalidate();
                return true;

            default:
                return true;
        }
    }

    /** Adds one smoothed piece of line from the previous point to (viewX, viewY). */
    private void segment(float viewX, float viewY, Paint paint) {
        pt[0] = viewX;
        pt[1] = viewY;
        toSheet.mapPoints(pt);
        float x = pt[0];
        float y = pt[1];
        float midX = (lastX + x) / 2f;
        float midY = (lastY + y) / 2f;
        path.quadTo(lastX, lastY, midX, midY);
        inkCanvas.drawPath(path, paint);
        path.reset();
        path.moveTo(midX, midY);
        lastX = x;
        lastY = y;
    }
}
