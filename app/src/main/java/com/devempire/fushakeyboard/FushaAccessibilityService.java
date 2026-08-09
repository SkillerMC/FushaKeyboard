package com.devempire.fushakeyboard;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Runs system-wide once the user enables it from Android's Accessibility
 * settings. It only acts when BubbleService asks it to: it grabs the text
 * from whichever field currently has input focus (in any app), sends it
 * for correction, and writes the result back into that same field.
 */
public class FushaAccessibilityService extends AccessibilityService {

    interface Callback {
        void onDone();

        void onError(String message);
    }

    private static FushaAccessibilityService instance;

    static FushaAccessibilityService getInstance() {
        return instance;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (instance == this) {
            instance = null;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // No-op: this service is purely on-demand, triggered from the bubble.
    }

    @Override
    public void onInterrupt() {
    }

    void convertFocusedText(Callback callback) {
        AccessibilityNodeInfo node = findFocusedEditableNode();
        if (node == null) {
            callback.onError("ما فيه حقل كتابة مفعّل حاليًا");
            return;
        }

        CharSequence current = node.getText();
        String text = current == null ? "" : current.toString().trim();
        if (text.isEmpty()) {
            callback.onError("الحقل فاضي");
            return;
        }

        String apiKey = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(MainActivity.PREF_API_KEY, "");

        if (apiKey.isEmpty()) {
            setNodeText(node, LocalFusha.convert(text));
            callback.onDone();
            return;
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        AiClient.correctToFusha(apiKey, text, new AiClient.Callback() {
            @Override
            public void onSuccess(String result) {
                mainHandler.post(() -> {
                    setNodeText(node, result);
                    callback.onDone();
                });
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> {
                    setNodeText(node, LocalFusha.convert(text));
                    callback.onDone();
                });
            }
        });
    }

    private AccessibilityNodeInfo findFocusedEditableNode() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return null;
        }
        return root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
    }

    private void setNodeText(AccessibilityNodeInfo node, String newText) {
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText);
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }
}
