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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Perspective;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ColorUtil;

public class RainbowRaveTileIndicatorsOverlay extends Overlay
{
	private final Client client;
	private final RainbowRaveTileIndicatorsPlugin plugin;
	private final RainbowRavePlugin rainbowRavePlugin;

	public RainbowRaveTileIndicatorsOverlay(Client client, RainbowRaveTileIndicatorsPlugin plugin, RainbowRavePlugin rainbowRavePlugin)
	{
		this.client = client;
		this.plugin = plugin;
		this.rainbowRavePlugin = rainbowRavePlugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(Overlay.PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		WorldView wv = client.getTopLevelWorldView();
		if (plugin.highlightHoveredTile)
		{
			// If we have tile "selected" render it
			if (wv.getSelectedSceneTile() != null)
			{
				renderTile(graphics, wv.getSelectedSceneTile().getLocalLocation(), rainbowRavePlugin.getColor(0), plugin.hoveredTileBorderWidth, plugin.hoveredTileFillColor, plugin.hoveredTileCornersOnly, plugin.hoveredTileCornerSize);
			}
		}

		if (plugin.highlightDestinationTile)
		{
			renderTile(graphics, client.getLocalDestinationLocation(), rainbowRavePlugin.getColor(0), plugin.destinationTileBorderWidth, plugin.destinationTileFillColor, plugin.destinationTileCornersOnly, plugin.destinationTileCornerSize);
		}

		if (plugin.highlightCurrentTile)
		{
			final LocalPoint playerPosLocal = LocalPoint.fromWorld(wv, plugin.getLastPlayerPosition());
			if (playerPosLocal == null)
			{
				return null;
			}

			Color rainbowColor = rainbowRavePlugin.getColor(0);
			Color color = ColorUtil.colorWithAlpha(rainbowColor, plugin.highlightCurrentColor.getAlpha());
			Color fillColor = plugin.recolorFill ? ColorUtil.colorWithAlpha(rainbowColor, plugin.currentTileFillColor.getAlpha()) : plugin.currentTileFillColor;
			// When not fading out the current tile, or when it has been 1 game tick or less since the player last
			// moved, draw the tile at full opacity. When using fadeout, drawing the indicator at full opacity for 1
			// game tick prevents it from fading out when moving on consecutive ticks.
			if (!plugin.trueTileFadeout || client.getTickCount() - plugin.getLastTickPlayerMoved() <= 1)
			{
				renderTile(graphics, playerPosLocal, color, plugin.currentTileBorderWidth, fillColor, plugin.currentTileCornersOnly, plugin.currentTileCornerSize);
			}
			else
			{
				// It is 1 game tick after the player has stopped moving, so fade out the tile.
				long timeSinceLastMove = System.currentTimeMillis() - plugin.getLastTimePlayerStoppedMoving();
				// The fadeout does not begin for 1 game tick, so subtract that.
				int fadeoutTime = plugin.trueTileFadeoutTime - Constants.GAME_TICK_LENGTH;
				if (fadeoutTime != 0 && timeSinceLastMove < fadeoutTime)
				{
					double opacity = 1.0d - Math.pow(timeSinceLastMove / (double) fadeoutTime, 2);
					renderTile(graphics, playerPosLocal, ColorUtil.colorWithAlpha(color, (int) (opacity * color.getAlpha())), plugin.currentTileBorderWidth, ColorUtil.colorWithAlpha(fillColor, (int) (opacity * fillColor.getAlpha())), plugin.currentTileCornersOnly, plugin.currentTileCornerSize);
				}
			}
		}

		return null;
	}

	private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color, final double borderWidth, final Color fillColor, boolean cornersOnly, int divisor)
	{
		if (dest == null)
		{
			return;
		}

		final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

		if (poly == null)
		{
			return;
		}

		if (cornersOnly)
		{
			renderPolygonCorners(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth), divisor);
		}
		else
		{
			OverlayUtil.renderPolygon(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth));
		}
	}

	public static void renderPolygonCorners(Graphics2D graphics, Polygon poly, Color color, Color fillColor, Stroke borderStroke, int divisor)
	{
		graphics.setColor(color);
		final Stroke originalStroke = graphics.getStroke();
		graphics.setStroke(borderStroke);

		for (int i = 0; i < poly.npoints; i++)
		{
			int ptx = poly.xpoints[i];
			int pty = poly.ypoints[i];
			int prev = (i - 1) < 0 ? (poly.npoints - 1) : (i - 1);
			int next = (i + 1) > (poly.npoints - 1) ? 0 : (i + 1);
			int ptxN = ((poly.xpoints[next]) - ptx) / divisor + ptx;
			int ptyN = ((poly.ypoints[next]) - pty) / divisor + pty;
			int ptxP = ((poly.xpoints[prev]) - ptx) / divisor + ptx;
			int ptyP = ((poly.ypoints[prev]) - pty) / divisor + pty;
			graphics.drawLine(ptx, pty, ptxN, ptyN);
			graphics.drawLine(ptx, pty, ptxP, ptyP);
		}

		graphics.setColor(fillColor);
		graphics.fill(poly);

		graphics.setStroke(originalStroke);
	}
}
