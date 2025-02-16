/*
 * Copyright (c) 2018, James Swindle <wilingua@gmail.com>
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

import com.rainbowrave.RainbowRaveNpcIndicatorsPlugin.HighlightedNpc;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.util.Text;

public class RainbowRaveNpcSceneOverlay extends Overlay
{
	// Anything but white text is quite hard to see since it is drawn on
	// a dark background
	private static final Color TEXT_COLOR = Color.WHITE;

	private static final NumberFormat TIME_LEFT_FORMATTER = DecimalFormat.getInstance(Locale.US);

	static
	{
		((DecimalFormat)TIME_LEFT_FORMATTER).applyPattern("#0.0");
	}

	private final Client client;
	private final NpcIndicatorsConfig config;
	private final RainbowRaveNpcIndicatorsPlugin plugin;
	private final RainbowRaveConfig rainbowRaveConfig;
	private final ModelOutlineRenderer modelOutlineRenderer;

	private final RainbowRavePlugin rainbowRavePlugin;

	RainbowRaveNpcSceneOverlay(Client client, NpcIndicatorsConfig config, RainbowRaveNpcIndicatorsPlugin plugin,
							   ModelOutlineRenderer modelOutlineRenderer, RainbowRavePlugin rainbowRavePlugin, RainbowRaveConfig rainbowRaveConfig)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.rainbowRavePlugin = rainbowRavePlugin;
		this.rainbowRaveConfig = rainbowRaveConfig;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (enabled)
		{
			if (config.showRespawnTimer())
			{
				plugin.getDeadNpcsToDisplay().forEach((id, npc) -> renderNpcRespawn(npc, graphics));
			}

			for (HighlightedNpc highlightedNpc : plugin.getHighlightedNpcs().values())
			{
				renderNpcOverlay(graphics, highlightedNpc, null);
			}
		}

		if (rainbowRaveConfig.highlightSelf() || rainbowRaveConfig.highlightOthers())
		{
			// The NPC here is never used, but HighlightedNpc requires it to be non-null.
			if (rainbowRaveConfig.highlightSelf())
			{
				renderNpcOverlay(graphics, plugin.highlightedPlayer(), client.getLocalPlayer());
			}
			if (rainbowRaveConfig.highlightOthers())
			{
				for (Player player : client.getTopLevelWorldView().players())
				{
					if (player != client.getLocalPlayer())
						renderNpcOverlay(graphics, plugin.highlightedPlayer(), player);
				}
			}
		}

		return null;
	}

	private void renderNpcRespawn(final MemorizedNpc npc, final Graphics2D graphics)
	{
		if (npc.getPossibleRespawnLocations().isEmpty())
		{
			return;
		}

		final WorldPoint respawnLocation = npc.getPossibleRespawnLocations().get(0);
		final LocalPoint lp = LocalPoint.fromWorld(npc.getWorldView(), respawnLocation.getX(), respawnLocation.getY());

		if (lp == null)
		{
			return;
		}

		final LocalPoint centerLp = new LocalPoint(
			lp.getX() + Perspective.LOCAL_TILE_SIZE * (npc.getNpcSize() - 1) / 2,
			lp.getY() + Perspective.LOCAL_TILE_SIZE * (npc.getNpcSize() - 1) / 2,
			npc.getWorldView());

		final Polygon poly = Perspective.getCanvasTileAreaPoly(client, centerLp, npc.getNpcSize());
		renderPoly(graphics, config.highlightColor(), config.fillColor(), poly);

		final Instant now = Instant.now();
		final double baseTick = ((npc.getDiedOnTick() + npc.getRespawnTime()) - client.getTickCount()) * (Constants.GAME_TICK_LENGTH / 1000.0);
		final double sinceLast = (now.toEpochMilli() - plugin.getLastTickUpdate().toEpochMilli()) / 1000.0;
		final double timeLeft = Math.max(0.0, baseTick - sinceLast);
		final String timeLeftStr = TIME_LEFT_FORMATTER.format(timeLeft);

		final int textWidth = graphics.getFontMetrics().stringWidth(timeLeftStr);
		final int textHeight = graphics.getFontMetrics().getAscent();

		final Point canvasPoint = Perspective
			.localToCanvas(client, centerLp, respawnLocation.getPlane());

		if (canvasPoint != null)
		{
			final Point canvasCenterPoint = new Point(
				canvasPoint.getX() - textWidth / 2,
				canvasPoint.getY() + textHeight / 2);

			OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, timeLeftStr, TEXT_COLOR);
		}
	}

	private void renderNpcOverlay(Graphics2D graphics, HighlightedNpc highlightedNpc, Player player)
	{
		Actor actor;
		NPCComposition npcComposition = null;
		if (player == null)
		{
			actor = highlightedNpc.getNpc();
			npcComposition = highlightedNpc.getNpc().getTransformedComposition();
			if (npcComposition == null || !npcComposition.isInteractible()
				|| (actor.isDead() && config.ignoreDeadNpcs()))
			{
				return;
			}
		} else {
			actor = player;
		}

		final Color borderColor = rainbowRavePlugin.getColor(actor.hashCode());
		Color fillColor = rainbowRavePlugin.getColor(actor.hashCode());
		fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), config.fillColor().getAlpha());

		if (highlightedNpc.isHull())
		{
			Shape objectClickbox = actor.getConvexHull();
			renderPoly(graphics, borderColor, fillColor, objectClickbox);
		}

		if (highlightedNpc.isTile())
		{
			int size = npcComposition == null ? 1 : npcComposition.getSize();
			LocalPoint lp = actor.getLocalLocation();
			Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);

			renderPoly(graphics, borderColor, fillColor, tilePoly);
		}

		if (highlightedNpc.isTrueTile())
		{
			LocalPoint lp = LocalPoint.fromWorld(actor.getWorldView(), actor.getWorldLocation()); // centered on sw tile
			if (lp != null)
			{
				int size = npcComposition == null ? 1 : npcComposition.getSize();
				final LocalPoint centerLp = new LocalPoint(
					lp.getX() + Perspective.LOCAL_TILE_SIZE * (size - 1) / 2,
					lp.getY() + Perspective.LOCAL_TILE_SIZE * (size - 1) / 2,
					actor.getWorldView());
				Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, centerLp, size);
				renderPoly(graphics, borderColor, fillColor, tilePoly);
			}
		}

		if (highlightedNpc.isSwTile())
		{
			int size = npcComposition == null ? 1 : npcComposition.getSize();
			LocalPoint lp = actor.getLocalLocation();

			int x = lp.getX() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);
			int y = lp.getY() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);

			Polygon southWestTilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y, actor.getWorldView()));
			renderPoly(graphics, borderColor, fillColor, southWestTilePoly);
		}

		if (highlightedNpc.isSwTrueTile())
		{
			LocalPoint lp = LocalPoint.fromWorld(actor.getWorldView(), actor.getWorldLocation());
			if (lp != null)
			{
				Polygon tilePoly = Perspective.getCanvasTilePoly(client, lp);
				renderPoly(graphics, borderColor, fillColor, tilePoly);
			}
		}

		if (highlightedNpc.isOutline())
		{
			if (player == null)
			{
				modelOutlineRenderer.drawOutline(highlightedNpc.getNpc(), (int) config.borderWidth(), borderColor, config.outlineFeather());
			} else {
				modelOutlineRenderer.drawOutline(player, (int) config.borderWidth(), borderColor, config.outlineFeather());
			}
		}

		if (highlightedNpc.isName() && actor.getName() != null)
		{
			String npcName = Text.removeTags(actor.getName());
			Point textLocation = actor.getCanvasTextLocation(graphics, npcName, actor.getLogicalHeight() + 40);

			if (textLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, textLocation, npcName, borderColor);
			}
		}
	}

	private void renderPoly(Graphics2D graphics, Color borderColor, Color fillColor, Shape polygon)
	{
		if (polygon != null)
		{
			graphics.setColor(borderColor);
			graphics.setStroke(new BasicStroke((float) config.borderWidth()));
			graphics.draw(polygon);
			graphics.setColor(fillColor);
			graphics.fill(polygon);
		}
	}

	private boolean enabled = true;
	public void enable(boolean enabled)
	{
		this.enabled = enabled;
	}
}
