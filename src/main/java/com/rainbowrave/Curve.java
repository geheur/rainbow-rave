package com.rainbowrave;

import java.util.ArrayList;
import net.runelite.api.Point;

public class Curve {

    private final Point from;
    private final Point to;
    private final ArrayList<Point> curve = new ArrayList<>();

    public Curve(Point from, Point to) {
        this.from = from;
        this.to = to;

        interpolate();
    }

    private void interpolate() {
        // TODO hook size of Curve into config
        for(int i = 1; i <= 5; i++) {
            curve.add(Interpolate.interpolate(from, to, (double) (i * 2) / 10));
        }
    }

    public Point getFrom() {
        return from;
    }

    public Point getTo() {
        return to;
    }

    public ArrayList<Point> getCurve() {
        return curve;
    }
}
