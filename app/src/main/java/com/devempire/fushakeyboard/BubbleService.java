package com.devempire.fushakeyboard;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class BubbleService extends Service {

    private static final String IDLE_LABEL = "ف";
    private static final String BUSY_LABEL = "...";

    private WindowManager windowManager;
    private Button bubble;
    private WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        bubble = new Button(this);
        bubble.setText(IDLE_LABEL);
        bubble.setTextSize(20);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 300;

        bubble.setOnTouchListener(new DragTouchListener());
        windowManager.addView(bubble, params);
    }

    private void onBubbleTapped() {
        FushaAccessibilityService service = FushaAccessibilityService.getInstance();
        if (service == null) {
            Toast.makeText(this, "فعّل خدمة الوصول (Accessibility) أولاً من التطبيق", Toast.LENGTH_LONG).show();
            return;
        }

        bubble.setText(BUSY_LABEL);
        bubble.setEnabled(false);

        service.convertFocusedText(new FushaAccessibilityService.Callback() {
            @Override
            public void onDone() {
                bubble.setText(IDLE_LABEL);
                bubble.setEnabled(true);
            }

            @Override
            public void onError(String message) {
                bubble.setText(IDLE_LABEL);
                bubble.setEnabled(true);
                Toast.makeText(BubbleService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bubble != null && windowManager != null) {
            windowManager.removeView(bubble);
        }
    }

    /** Lets the bubble be dragged around the screen, and treats a non-dragging tap as a click. */
    private class DragTouchListener implements View.OnTouchListener {
        private int startX, startY;
        private float startTouchX, startTouchY;
        private boolean dragged;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = params.x;
                    startY = params.y;
                    startTouchX = event.getRawX();
                    startTouchY = event.getRawY();
                    dragged = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) (event.getRawX() - startTouchX);
                    int dy = (int) (event.getRawY() - startTouchY);
                    if (Math.abs(dx) > 12 || Math.abs(dy) > 12) {
                        dragged = true;
                    }
                    params.x = startX + dx;
                    params.y = startY + dy;
                    windowManager.updateViewLayout(bubble, params);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!dragged) {
                        onBubbleTapped();
                    }
                    return true;
                default:
                    return false;
            }
        }
    }
}
