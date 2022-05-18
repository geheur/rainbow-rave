/*
 * Copyright (c) 2022, Ryan Bell <llaver@live.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rainbowrave;

import java.util.ArrayList;
import java.util.List;

import net.runelite.api.Point;

public class Curve {

    private final Point from;
    private final Point to;
    private final List<Point> curve = new ArrayList<>();

    public Curve(Point from, Point to) {
        this.from = from;
        this.to = to;

        // TODO hook size of Curve into config
        for(int i = 1; i <= 5; i++) {
            curve.add(interpolate(from, to, (double) (i * 2) / 10));
        }
    }

    /**
     * Linearly interpolates between two points.
     *
     * @param from The starting point.
     * @param to The ending point.
     * @param t The interpolation progress starting at 0 and going to 1 (percent of distance between points).
     * @return The interpolated point.
     */
    private Point interpolate(Point from, Point to, double t) {
        double x = from.getX() * (1 - t) + to.getX() * t;
        double y = from.getY() * (1 - t) + to.getY() * t;
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    public Point getFrom() {
        return from;
    }

    public Point getTo() {
        return to;
    }

    public List<Point> getCurve() {
        return curve;
    }
}
