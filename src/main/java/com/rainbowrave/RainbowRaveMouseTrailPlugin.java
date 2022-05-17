/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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

import java.util.*;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.MouseManager;

@Slf4j
public class RainbowRaveMouseTrailPlugin
{
    private final ArrayDeque<Curve> curve = new ArrayDeque<>();
    private Point temp = null;

    @Inject
    private MouseManager mouseManager;

    private RainbowRaveMouseListener mouseListener;

    protected void startUp()
    {
        mouseListener = new RainbowRaveMouseListener(this);
        setMouseListenerEnabled(true);
    }

    protected void shutDown()
    {
        curve.clear();

        setMouseListenerEnabled(false);
        mouseListener = null;
    }

    public void setMouseListenerEnabled(boolean enabled)
    {
        if (enabled)
        {
            mouseManager.registerMouseListener(mouseListener);
        }
        else
        {
            mouseManager.unregisterMouseListener(mouseListener);
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

        if (configChanged.getGroup().equals("rainbow_rave") && configChanged.getKey().equals("whichMouseTrailStyle")) {
            System.out.println("Enabled: " + !configChanged.getNewValue().equals("NONE"));
            setMouseListenerEnabled(!configChanged.getNewValue().equals("NONE"));
        }
    }

    public void updateMousePositions(Point point) {
            if (curve.size() < 50) {
                if (temp != null) {
                    Curve current = new Curve(temp, point);
                    curve.add(current);
                }
                temp = point;
            }
    }

    public ArrayDeque<Curve> getTrail() {
        return curve;
    }

    public void popTrail() {
        if(curve.size() > 0) {
            curve.pop();
        }
    }
}