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

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.MouseManager;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
public class RainbowRaveMouseTrailPlugin
{
    private static final String CONFIG_GROUP = "objectindicators";
    private static final String MARK = "Mark object";
    private static final String UNMARK = "Unmark object";

    private final ArrayDeque<Curve> curve = new ArrayDeque<>();

    private Point temp = null;

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private MouseManager mouseManager;

    @Inject
    private Gson gson;

    @Inject
    private RainbowRaveConfig rainbowRaveConfig;

    @Inject
    private ClientThread clientThread;

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

    public void updateMousePositions(Point point) {
        if(temp == null) {
            temp = point;
        } else {
            curve.add(new Curve(temp, point));
            temp = point;
        }

        // TODO Config - Size?
        if(curve.size() > 100) {
            curve.pop();
        }
    }

    public ArrayDeque<Curve> getTrail() {
        return curve;
    }

    public void popTrail() {
        curve.pop();
    }
}