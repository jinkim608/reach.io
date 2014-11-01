package io.reach.reachiodemo.Bus;

/**
 * Created by Jin on 10/31/14.
 * <p/>
 * This event is used to deliver x and y position of the selector from the service to main activity
 */
public class SelectorLocationEvent {
    public final int x;
    public final int y;

    public SelectorLocationEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
