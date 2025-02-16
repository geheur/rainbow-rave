/*
 * Copyright (c) 2018, TheLonelyDev <https://github.com/TheLonelyDev>
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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Random;
import javax.annotation.Nullable;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.groundmarkers.GroundMarkerConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class RainbowRaveGroundMarkerOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 32;

	private final Client client;
	private final GroundMarkerConfig config;
	private final RainbowRaveGroundMarkerPlugin plugin;
	private final RainbowRaveConfig rainbowRaveConfig;
	private final RainbowRavePlugin rainbowRavePlugin;

	public RainbowRaveGroundMarkerOverlay(Client client, GroundMarkerConfig config, RainbowRaveGroundMarkerPlugin plugin, RainbowRavePlugin rainbowRavePlugin, RainbowRaveConfig rainbowRaveConfig)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		this.rainbowRavePlugin = rainbowRavePlugin;
		this.rainbowRaveConfig = rainbowRaveConfig;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	private Random random = new Random();

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!rainbowRaveConfig.rainbowTileMarkers()) return null;

		final Collection<ColorTileMarker> points = plugin.getPoints();
		if (points.isEmpty())
		{
			return null;
		}

		Stroke stroke = new BasicStroke((float) config.borderWidth());
		try
		{
			int plane = client.getTopLevelWorldView().getPlane();
			for (final ColorTileMarker point : points)
			{
				WorldPoint worldPoint = point.getWorldPoint();
				if (worldPoint.getPlane() != plane)
				{
					continue;
				}

				// This formula does not have a solid concept behind it, I just tried random stuff.
				int hashCode;
				switch (rainbowRaveConfig.groundMarkerColorMode()) {
					case WAVES:
						hashCode = (point.getWorldPoint().getX() + point.getWorldPoint().getY()) * 5;
						break;
					case COLOR_SYNC:
						if (point.getColor() != null) {
							random.setSeed(point.getColor().getRGB());
							hashCode = random.nextInt(1000);
							break;
						}
					case RANDOM:
					default:
						random.setSeed(((long) point.getWorldPoint().getX() << 16) + point.getWorldPoint().getY());
						hashCode = random.nextInt(1000);
				}
				Color tileColor = rainbowRaveConfig.syncColor() ? rainbowRavePlugin.getColor(0) : rainbowRaveConfig.theme().getColor(((hashCode + (client.getGameCycle() * (6000f / rainbowRaveConfig.colorSpeed()))) % 300) / 300f);
				drawTile(graphics, worldPoint, tileColor, point.getLabel(), stroke, rainbowRaveConfig.fillTiles());
			}
		} catch (ConcurrentModificationException e) {
			// can happen when removing stuff from points, such as when disabling drawing brush markers tiles.
			// don't care, doesn't have a significant visual effect.
		}

		return null;
	}

	private void drawTile(Graphics2D graphics, WorldPoint point, Color color, @Nullable String label, Stroke borderStroke, boolean fill)
	{
		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		if (point.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
		{
			return;
		}

		LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), point);
		if (lp == null)
		{
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, lp);
		if (poly != null)
		{
			if (fill)
			{
				graphics.setColor(color);
				graphics.fillPolygon(poly);
			}
			graphics.setColor(color);
			Stroke originalStroke = graphics.getStroke();
			graphics.setStroke(borderStroke);
			graphics.draw(poly);
			graphics.setStroke(originalStroke);
		}

		if (!Strings.isNullOrEmpty(label))
		{
			Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, label, 0);
			if (canvasTextLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, canvasTextLocation, label, color);
			}
		}
	}
}
