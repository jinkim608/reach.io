package io.reach.reachiodemo;

import android.app.Application;

/**
 * Created by Jin on 10/30/14.
 * <p/>
 * Singleton App for Reach.io application
 * - holds global variables and app states
 */
public class App extends Application {
    private static App mInstance = null;
    public int anchorSize;
    public int anchorOuterMargin;
    public int selectorSize;

    public float movementRate;

    private App() {

        /* dimension variables */
        anchorSize = 100;
        anchorOuterMargin = 20;
        selectorSize = 60;

        movementRate = 2.0f;


    }


    public static App getInstance() {
        if (mInstance == null) {
            mInstance = new App();
        }
        return mInstance;
    }
}
