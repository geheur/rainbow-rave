package com.rainbowrave;

import net.runelite.api.Point;

// Helper class for interpolate functions
// More can be added, then chosen from a dropdown in config
public class Interpolate {

    /**
     * Linearly interpolates between two points.
     *
     * @param from The starting point.
     * @param to The ending point.
     * @param t The interpolation progress starting at 0 and going to 1.
     * @return The interpolated point.
     */
    public static Point interpolate(Point from, Point to, double t) {
        double x = from.getX() * (1 - t) + to.getX() * t;
        double y = from.getY() * (1 - t) + to.getY() * t;
        return new Point((int) Math.round(x), (int) Math.round(y));
    }
}
