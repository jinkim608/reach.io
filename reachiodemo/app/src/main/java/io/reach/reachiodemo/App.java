package io.reach.reachiodemo;

import android.app.Application;

/**
 * Created by Jinhyun Kim, Muzi Li
 * https://github.com/jinkim608/reach.io
 * <p/>
 * Singleton App for Reach.io application that holds global variables and app states
 */
public class App extends Application {
    private static App mInstance = null;
    public int anchorSize;
    public int anchorOuterMargin;
    public int selectorSize;
    public int thumbSize;
    public int dropRegionSize;

    public float movementRate;
    public final long RESET_DELAY = 2000; // delay in ms before resetting indicators
    public final long LONGCLICK_DELAY = 1000; // threshold in ms before long click trigger


    private App() {

        /* dimension variables */
        anchorSize = 155;
        anchorOuterMargin = 20;

        // include invisible padding size for drop event
        dropRegionSize = anchorSize + anchorOuterMargin * 2 + 100;

        selectorSize = 101;

        thumbSize = 131;

        movementRate = 2.0f;
    }

    public static App getInstance() {
        if (mInstance == null) {
            mInstance = new App();
        }
        return mInstance;
    }

}