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

import java.awt.event.MouseEvent;
import java.util.*;
import java.util.Deque;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;

@Slf4j
public class RainbowRaveMouseTrailPlugin
{
    private final Deque<Curve> curves = new ArrayDeque<>();
    private Point temp = null;
    private int trailLength = 0;

    @Inject
    private MouseManager mouseManager;

    @Inject
    private RainbowRaveConfig config;

    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public MouseEvent mouseMoved(MouseEvent event)
        {
            updateMousePositions(new Point(event.getX(), event.getY()));
            return event;
        }
        @Override
        public MouseEvent mouseDragged(MouseEvent event)
        {
            updateMousePositions(new Point(event.getX(), event.getY()));
            return event;
        }
    };

    protected void startUp()
    {
        trailLength = 0;
        setMouseListenerEnabled(true);
    }

    protected void shutDown()
    {
        curves.clear();
        trailLength = 0;
        setMouseListenerEnabled(false);
    }

    protected void restart()
    {
        shutDown();
        startUp();
    }

    public void setMouseListenerEnabled(boolean enabled)
    {
        if (enabled)
        {
            mouseManager.registerMouseListener(mouseAdapter);
        }
        else
        {
            mouseManager.unregisterMouseListener(mouseAdapter);
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        popTrail();
        popTrail();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (!configChanged.getGroup().equals("rainbow_rave")){
            return;
        }
        setMouseListenerEnabled(config.whichMouseTrailStyle() != RainbowRaveConfig.MouseTrailStyle.NONE);
        if (configChanged.getKey().equals("mouseTrailLength")) {
            restart();
        }
    }

    public void updateMousePositions(Point point) {
            if (curves.size() < config.mouseTrailLength()) {
                if (temp != null) {
                    Curve current = new Curve(temp, point);
                    curves.add(current);
                    trailLength += current.getCurve().size();
                }
                temp = point;
            }
    }

    public Deque<Curve> getTrail() {
        return curves;
    }

    public int getTrailLength() {
        return trailLength;
    }

    public void popTrail() {
        if(curves.size() > 0) {
            trailLength -= curves.pop().getCurve().size();
        }
    }
}