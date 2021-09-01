package com.rainbowrave;

import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class RainbowRaveOverlay extends Overlay
{
	// TODO check render order of this vs ground markers.

	@Override
	public Dimension render(Graphics2D graphics)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		return null;
	}
}
