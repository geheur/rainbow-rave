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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

class RainbowRaveMouseTrailOverlay extends Overlay
{
    private final RainbowRavePlugin rainbowRavePlugin;
    private final RainbowRaveMouseTrailPlugin plugin;
    private final RainbowRaveConfig rainbowRaveConfig;

    public RainbowRaveMouseTrailOverlay(RainbowRavePlugin rainbowRavePlugin, RainbowRaveMouseTrailPlugin plugin, RainbowRaveConfig rainbowRaveConfig)
    {
        this.plugin = plugin;
        this.rainbowRavePlugin = rainbowRavePlugin;
        this.rainbowRaveConfig = rainbowRaveConfig;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    // Function the get the rainbow color for a particular point
    private Color getColor(int position) {
        // TODO hook curve size multiplier into config
        float currentPercent = 0;
        switch (rainbowRaveConfig.whichMouseTrailStyle()) {
            case SYNCED:
                return rainbowRavePlugin.getColor(0);
            case PARTYMODE:
                // This allows the color cycle to repeat 3 times for every 50 curves
                // and allowing party mode to have a similar effect regardless of the trail length
                float partyScale = (plugin.getTrail().size() / 50f) * 3;
                currentPercent = position / (plugin.getTrailLength() / partyScale);
                break;
            case ENABLED:
                // Calculate how far into the trail this point position is
                currentPercent = (float) position / plugin.getTrailLength();
                // Scale the percent by 7/8 to stop colors from repeating on the ends of the trail
                currentPercent *= (7f/8f);
                break;
        }
        return rainbowRaveConfig.theme().getColor(currentPercent);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Disable overlay if mouse trail style set to None
        if (rainbowRaveConfig.whichMouseTrailStyle() == RainbowRaveConfig.MouseTrailStyle.NONE) {
            return null;
        }
        // Get ArrayList of Curves
        List<Curve> trail = new ArrayList<>(plugin.getTrail());
        // Points to track where to render a line between Curves
        Point midBefore = null;
        Point midAfter = null;

        // Set trail size, stroke, and antialiasing
        graphics.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Fallback default color
        graphics.setColor(Color.BLUE);
        // Position keeps track of how many points have been iterated over
        int position = 0;
        // Loop through Curves of trail
        for(int i = 0; i < trail.size(); i++) {
            // Get Points from Curve
            List<Point> points = trail.get(i).getCurve();
            // Loop through points
            for(int j = 0; j < points.size(); j++ ) {

                // Initialize points used to calculate parts of trail with null
                Point before = null;
                Point after = null;
                Point previous = null;
                Point current = null;
                // Logic for setting points depending on where in the loops we are
                if(i != 0 && j == 0) {
                    // Get previous list of points from the previous Curve
                    List<Point> previousPoints = trail.get(i - 1).getCurve();
                    before = previousPoints.get(previousPoints.size() - 2);
                    after = points.get(j + 1);
                    previous = previousPoints.get(previousPoints.size() - 1);
                    current = points.get(j);
                } else if(j > 1) {
                    // Set points from current Curve
                    before = points.get(j - 2);
                    previous = points.get(j - 1);
                    current = points.get(j);
                    // If the last point in the Curve, and not the last curve in the trail, get second point from next Curve
                    if(i < trail.size() - 1 && j == points.size() - 1) {
                        after = trail.get(i + 1).getCurve().get(1);
                    }
                }
                // Get second to last in previous Curve and third point in current Curve
                // Will be used to draw another line due to tiny gaps left in between Curves
                if(j == 4) {
                    if (midBefore == null) {
                        midBefore = points.get(j);
                    }
                } else if (j == 2 && midBefore != null) {
                        midAfter = points.get(j);
                }
                // Get the rainbow color from helper method based on current position.
                final Color color = getColor(position);
                graphics.setColor(color);

                // Draw lines of the trail
                // We are drawing three to fill in gaps and slight inconsistencies
                // Still a small bug when drawing large circles very quickly
                // Causes lines to occasionally overlap at the edges
                if(previous != null && current != null) {
                    graphics.drawLine(previous.getX(), previous.getY(), current.getX(), current.getY());
                }
                if(before != null && after != null) {
                    graphics.drawLine(before.getX(), before.getY(), after.getX(), after.getY());
                }
                if(midBefore != null && midAfter != null) {
                    graphics.drawLine(midBefore.getX(), midBefore.getY(), midAfter.getX(), midAfter.getY());
                    // Progress mid points
                    midBefore = midAfter;
                    midAfter = null;
                }
                position++;
            }
        }
        return null;
    }
}