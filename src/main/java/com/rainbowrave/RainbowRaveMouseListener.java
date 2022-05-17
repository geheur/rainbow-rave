package com.rainbowrave;

import java.awt.event.MouseEvent;

import net.runelite.api.Point;
import net.runelite.client.input.MouseAdapter;

class RainbowRaveMouseListener extends MouseAdapter
{
    private final RainbowRaveMouseTrailPlugin plugin;

    RainbowRaveMouseListener(RainbowRaveMouseTrailPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event)
    {
        plugin.updateMousePositions(new Point(event.getX(), event.getY()));

        return event;
    }
}
