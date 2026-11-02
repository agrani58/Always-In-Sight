package com.agrani.seeitdoit;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * A see-through writing sheet that opens over your home screen when you tap the widget.
 * Write with the pen, press Done, and the widget shows your handwriting.
 */
public class WriteActivity extends Activity {

    private DrawView today;
    private DrawView week;
    private DrawView active;
    private boolean eraser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = dp(8);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        // ---- top bar: Pen/Eraser, Undo, Clear ............ Done ----
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        final Button modeBtn = button(getString(R.string.pen));
        Button undoBtn = button(getString(R.string.undo));
        Button clearBtn = button(getString(R.string.clear));
        Button doneBtn = button(getString(R.string.done));

        View spacer = new View(this);

        bar.addView(modeBtn);
        bar.addView(undoBtn);
        bar.addView(clearBtn);
        bar.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));
        bar.addView(doneBtn);
        root.addView(bar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- the two sheets: side by side on wide screens, stacked on tall ones ----
        DisplayMetrics dm = getResources().getDisplayMetrics();
        boolean wide = dm.widthPixels >= dm.heightPixels;

        today = new DrawView(this);
        week = new DrawView(this);
        today.load(Sheet.loadInk(this, Sheet.TODAY));
        week.load(Sheet.loadInk(this, Sheet.WEEK));
        today.setOnTouchedListener(() -> setActive(today));
        week.setOnTouchedListener(() -> setActive(week));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(wide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        content.addView(column(getString(R.string.today), today), cell(wide));
        content.addView(column(getString(R.string.week), week), cell(wide));
        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        setActive(today);

        // ---- button actions ----
        modeBtn.setOnClickListener(v -> {
            eraser = !eraser;
            today.setEraser(eraser);
            week.setEraser(eraser);
            modeBtn.setText(eraser ? R.string.eraser : R.string.pen);
        });
        undoBtn.setOnClickListener(v -> active.undo());
        clearBtn.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setMessage(getString(R.string.clear_question,
                        active == today ? getString(R.string.today) : getString(R.string.week)))
                .setPositiveButton(R.string.clear, (d, w) -> active.clearAll())
                .setNegativeButton(R.string.cancel, null)
                .show());
        doneBtn.setOnClickListener(v -> {
            saveAll();
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveAll(); // never lose handwriting, even if you swipe away
    }

    // ---- saving ------------------------------------------------------------

    private void saveAll() {
        boolean a = save(today, Sheet.TODAY);
        boolean b = save(week, Sheet.WEEK);
        if (a || b) {
            SeeItWidget.updateAll(this);
        }
    }

    private boolean save(DrawView view, String name) {
        if (view == null || !view.isDirty()) return false;
        try {
            Sheet.saveInk(this, name, view.getInk());
            view.markSaved();
            return true;
        } catch (IOException e) {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    // ---- small UI helpers ----------------------------------------------------

    private void setActive(DrawView v) {
        active = v;
        today.setActive(v == today);
        week.setActive(v == week);
    }

    private LinearLayout column(String title, DrawView view) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(4), dp(4), dp(4), dp(4));

        TextView label = new TextView(this);
        label.setText(title);
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setPadding(dp(8), dp(4), dp(8), dp(4));

        col.addView(label, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        col.addView(view, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        return col;
    }

    private LinearLayout.LayoutParams cell(boolean wide) {
        return wide
                ? new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                : new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        return b;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }
}
