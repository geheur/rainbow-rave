/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
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

import com.google.common.base.Strings;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.LinkedList;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

class RainbowRaveMouseTrailOverlay extends Overlay
{
    private final Client client;
    private final RainbowRavePlugin rainbowRavePlugin;
    private final RainbowRaveMouseTrailPlugin plugin;
    private final RainbowRaveConfig rainbowRaveConfig;

    public RainbowRaveMouseTrailOverlay(Client client, RainbowRavePlugin rainbowRavePlugin, RainbowRaveMouseTrailPlugin plugin, RainbowRaveConfig rainbowRaveConfig)
    {
        this.client = client;
        this.plugin = plugin;
        this.rainbowRavePlugin = rainbowRavePlugin;
        this.rainbowRaveConfig = rainbowRaveConfig;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.MED);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        ArrayDeque<Curve> trail = plugin.getTrail();
        int size = trail.size() * 10; // TODO hook multiplier into config
        graphics.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.BLUE);
        for(Curve curve: trail) {
            LinkedList<Point> points = curve.getCurve();
            for(int i = 1; i <= 10; i += 1 ) {
                Point previous = points.get(i - 1);
                Point current = points.get(i);

                final Color color = rainbowRavePlugin.getColor(current.hashCode());
                graphics.setColor(color);
                graphics.drawLine(previous.getX(), previous.getY(), current.getX(), current.getY());
            }
        }
        return null;
    }
}