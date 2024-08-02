package com.sunmi.subscreendemo;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;

public class SubDisplayManager {
    private SubDisplayManager() {
    }

    private static class SingletonInstance {
        private static final SubDisplayManager sInstance = new SubDisplayManager();
    }

    public static SubDisplayManager getInstance() {
        return SingletonInstance.sInstance;
    }

    private static final String TAG = "darren-SubDisplayManager";
    public DisplayManager displayManager = null;

    public static void init(Context context) {
        getInstance().displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        for (Display display : getSubDisplays()) {
            Log.d(TAG, "init: display: " + display.getDisplayId() + " - " + display.getName() + ":" + display);
        }
    }

    public static void release() {
    }

    public static DisplayManager getDisplayManager() {
        return getInstance().displayManager;
    }

    public static Display[] getSubDisplays() {
        return getDisplayManager().getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
    }

    public static String[] getSubDisplayIdAndName() {
        Display[] subDisplays = getSubDisplays();
        String[] names = new String[subDisplays.length];
        for (int i = 0; i < subDisplays.length; i++) {
            names[i] = subDisplays[i].getDisplayId() + "-" + subDisplays[i].getName();
        }
        return names;
    }

    public static Display getDisplayById(int id) {
        return getDisplayManager().getDisplay(id);
    }

    public static Display getDisplayByName(String name) {
        Display display = null;
        if (name != null) {
            for (Display subDisplay : getSubDisplays()) {
                if (name.equals(subDisplay.getName())) {
                    display = subDisplay;
                    break;
                }
            }
        }
        return display;
    }
}
