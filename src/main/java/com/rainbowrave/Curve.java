package com.rainbowrave;

import java.util.LinkedList;

import net.runelite.api.Point;

public class Curve {

    private final Point from;
    private final Point to;
    private final LinkedList<Point> curve = new LinkedList<>();

    public Curve(Point from, Point to) {
        this.from = from;
        this.to = to;

        interpolate();
    }

    private void interpolate() {
        // TODO hook size of loop into config
        for(int i = 0; i <= 10; i++) {
            curve.add(Interpolate.interpolate(from, to, (double) i / 10));
        }
    }

    public Point getFrom() {
        return from;
    }

    public Point getTo() {
        return to;
    }

    public LinkedList<Point> getCurve() {
        return curve;
    }
}
