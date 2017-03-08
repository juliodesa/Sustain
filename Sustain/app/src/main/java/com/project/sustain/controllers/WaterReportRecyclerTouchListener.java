package com.project.sustain.controllers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by georgiainstituteoftechnology on 3/7/17.
 */

// TODO: 3/8/17 Ask Anish/Julio about this, why is this used?  Where did this come from?   
public class WaterReportRecyclerTouchListener implements RecyclerView.OnItemTouchListener {
    private GestureDetector gestureDetector;
    private ClickListener clickListener;

    public WaterReportRecyclerTouchListener(Context context, final RecyclerView wtrRepRecyclerView, final ClickListener clickListener) {
        this.clickListener = clickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
            @Override
            public void onLongPress(MotionEvent e) {
                View child = wtrRepRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null && clickListener != null) {
                    clickListener.onLongClick(child, wtrRepRecyclerView.getChildPosition(child));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView wtrRepRecView, MotionEvent e) {
        View child = wtrRepRecView.findChildViewUnder(e.getX(), e.getY());
        if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
            clickListener.onClick(child, wtrRepRecView.getChildPosition(child));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView wtrRepRecView, MotionEvent e) {}

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}

    public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }
}