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

import java.awt.Color;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
public class RainbowRaveTileIndicatorsPlugin
{
	@Inject private Client client;
	@Inject private OverlayManager overlayManager;
	@Inject private ConfigManager configManager;
	@Inject private RainbowRaveConfig rainbowRaveConfig;

	@Getter private WorldPoint lastPlayerPosition = new WorldPoint(0, 0, 0);
	@Getter private int lastTickPlayerMoved = 0;
	@Getter private long lastTimePlayerStoppedMoving = 0;

	boolean recolorFill;

	boolean highlightDestinationTile;
	Color highlightDestinationColor;
	Color destinationTileFillColor;
	double destinationTileBorderWidth;
	boolean highlightHoveredTile;
	Color highlightHoveredColor;
	Color hoveredTileFillColor;
	double hoveredTileBorderWidth;
	boolean highlightCurrentTile;
	Color highlightCurrentColor;
	Color currentTileFillColor;
	double currentTileBorderWidth;

	boolean trueTileFadeout;
	int trueTileFadeoutTime;
	boolean destinationTileCornersOnly;
	boolean hoveredTileCornersOnly;
	boolean currentTileCornersOnly;
	int hoveredTileCornerSize;
	int destinationTileCornerSize;
	int currentTileCornerSize;

	protected void startUp()
	{
		loadConfig();
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged e) {
		loadConfig();
	}

	protected void shutDown()
	{
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e) {
		// I don't want this to apply to profile change config changes because the config is in an invalid state
		// during a profile change, so I check the thread since the EDT is where normal config changes happen.
		if (!SwingUtilities.isEventDispatchThread()) return;
		if (e.getGroup().equals("cornertileindicators") || e.getGroup().equals("tileindicators") || e.getGroup().equals("rainbow_rave"))
		{
			loadConfig();
		}
	}

	private void loadConfig() {
		boolean cornerTileIndicatorsInstalled = configManager.getConfiguration("runelite", "externalPlugins").contains("corner-tile-indicators");
		String group = cornerTileIndicatorsInstalled && rainbowRaveConfig.preferCornerTileIndicatorsConfig() ? "cornertileindicators" : "tileindicators";

		recolorFill = rainbowRaveConfig.recolorTileIndicatorFill();

		highlightDestinationTile = configManager.getConfiguration(group, "highlightDestinationTile", boolean.class);
		highlightDestinationColor = configManager.getConfiguration(group, "highlightDestinationColor", Color.class);
		destinationTileFillColor = configManager.getConfiguration(group, "destinationTileFillColor", Color.class);
		destinationTileBorderWidth = configManager.getConfiguration(group, "destinationTileBorderWidth", double.class);
		highlightHoveredTile = configManager.getConfiguration(group, "highlightHoveredTile", boolean.class);
		highlightHoveredColor = configManager.getConfiguration(group, "highlightHoveredColor", Color.class);
		hoveredTileFillColor = configManager.getConfiguration(group, "hoveredTileFillColor", Color.class);
		hoveredTileBorderWidth = configManager.getConfiguration(group, "hoveredTileBorderWidth", double.class);
		highlightCurrentTile = configManager.getConfiguration(group, "highlightCurrentTile", boolean.class);
		highlightCurrentColor = configManager.getConfiguration(group, "highlightCurrentColor", Color.class);
		currentTileFillColor = configManager.getConfiguration(group, "currentTileFillColor", Color.class);
		currentTileBorderWidth = configManager.getConfiguration(group, "currentTileBorderWidth", double.class);

		if ("cornertileindicators".equals(group))
		{
			trueTileFadeout = configManager.getConfiguration(group, "trueTileFadeout", boolean.class);
			trueTileFadeoutTime = configManager.getConfiguration(group, "trueTileFadeoutTime", int.class);
			destinationTileCornersOnly = configManager.getConfiguration(group, "destinationTileCornersOnly", boolean.class);
			hoveredTileCornersOnly = configManager.getConfiguration(group, "hoveredTileCornersOnly", boolean.class);
			currentTileCornersOnly = configManager.getConfiguration(group, "currentTileCornersOnly", boolean.class);
			hoveredTileCornerSize = configManager.getConfiguration(group, "hoveredTileCornerSize", int.class);
			destinationTileCornerSize = configManager.getConfiguration(group, "destinationTileCornerSize", int.class);
			currentTileCornerSize = configManager.getConfiguration(group, "currentTileCornerSize", int.class);
		} else {
			trueTileFadeout = false;
			destinationTileCornersOnly = false;
			hoveredTileCornersOnly = false;
			currentTileCornersOnly = false;
		}
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();

		if (!playerPos.equals(lastPlayerPosition))
		{
			lastTickPlayerMoved = client.getTickCount();
		}
		else if (lastTickPlayerMoved + 1 == client.getTickCount())
		{
			lastTimePlayerStoppedMoving = System.currentTimeMillis();
		}

		lastPlayerPosition = playerPos;
	}
}
