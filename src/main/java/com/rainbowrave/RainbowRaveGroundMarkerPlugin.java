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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.groundmarkers.GroundMarkerConfig;
import net.runelite.client.ui.overlay.OverlayManager;
import static net.runelite.http.api.RuneLiteAPI.GSON;

@Slf4j
public class RainbowRaveGroundMarkerPlugin
{
	private static final String CONFIG_GROUP = "groundMarker";
	private static final String MARK = "Mark tile";
	private static final String UNMARK = "Unmark tile";
	private static final String LABEL = "Label tile";
	private static final String WALK_HERE = "Walk here";
	private static final String REGION_PREFIX = "region_";

	private static final String BRUSH_CONFIG_GROUP = "brushMarkers";

	@Getter(AccessLevel.PACKAGE)
	private final List<ColorTileMarker> points = new ArrayList<>();

	@Inject
	private Client client;

	@Inject
	private GroundMarkerConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	private RainbowRaveConfig rainbowRaveConfig;

	@Inject
	private EventBus eventBus;

	@Inject
	private Gson gson;

	Collection<GroundMarkerPoint> getPoints(int regionId)
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, REGION_PREFIX + regionId);
		if (Strings.isNullOrEmpty(json))
		{
			return Collections.emptyList();
		}

		// CHECKSTYLE:OFF
		return gson.fromJson(json, new TypeToken<List<GroundMarkerPoint>>(){}.getType());
		// CHECKSTYLE:ON
	}

	void loadPoints()
	{
		loadPoints(null, false);
	}

	void loadPoints(GroundMarkerPoint extraPoint, boolean added)
	{
		points.clear();

		int[] regions = client.getMapRegions();

		if (regions == null)
		{
			return;
		}

		for (int regionId : regions)
		{
			// load points for region
			log.debug("Loading points for region {}", regionId);
			Collection<GroundMarkerPoint> regionPoints = new ArrayList<>(getPoints(regionId));
			if (extraPoint != null && !added)
			{
				regionPoints.remove(extraPoint);
			}
			Collection<ColorTileMarker> colorTileMarkers = translateToColorTileMarker(regionPoints);
			points.addAll(colorTileMarkers);

			if (extraPoint != null && added) {
				colorTileMarkers = translateToColorTileMarker(Collections.singletonList(extraPoint));
				points.addAll(colorTileMarkers);
			}

			if (rainbowRaveConfig.useBrushMarkerTiles())
			{
				Collection<GroundMarkerPoint> brushRegionPoints = getBrushPoints(regionId);
				Collection<ColorTileMarker> brushColorTileMarkers = translateToColorTileMarker(brushRegionPoints);
				points.addAll(brushColorTileMarkers);
			}
		}
	}

	/**
	 * Translate a collection of ground marker points to color tile markers, accounting for instances
	 *
	 * @param points {@link GroundMarkerPoint}s to be converted to {@link ColorTileMarker}s
	 * @return A collection of color tile markers, converted from the passed ground marker points, accounting for local
	 *         instance points. See {@link WorldPoint#toLocalInstance(Client, WorldPoint)}
	 */
	private Collection<ColorTileMarker> translateToColorTileMarker(Collection<GroundMarkerPoint> points)
	{
		if (points.isEmpty())
		{
			return Collections.emptyList();
		}

		return points.stream()
			.map(point -> new ColorTileMarker(
				WorldPoint.fromRegion(point.getRegionId(), point.getRegionX(), point.getRegionY(), point.getZ()),
				point.getColor(), point.getLabel()))
			.flatMap(colorTile ->
			{
				final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, colorTile.getWorldPoint());
				return localWorldPoints.stream().map(wp -> new ColorTileMarker(wp, colorTile.getColor(), colorTile.getLabel()));
			})
			.collect(Collectors.toList());
	}

	public void startUp()
	{
		loadPoints();
	}

	public void shutDown()
	{
		points.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		// map region has just been updated
		loadPoints();
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction().getId() != MenuAction.RUNELITE.getId())
		{
			return;
		}

		Tile target = client.getSelectedSceneTile();
		if (target == null)
		{
			return;
		}

		final String option = event.getMenuOption();
		if (option.equals(MARK) || option.equals(UNMARK))
		{
			markTile(target.getLocalLocation());
		}
		else if (option.equals(LABEL))
		{
			labelTile(target);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(RainbowRavePlugin.GROUP) && event.getKey().equals("useBrushMarkerTiles")) {
			loadPoints();
		}
		if (event.getGroup().equals(BRUSH_CONFIG_GROUP) && event.getKey().startsWith(REGION_PREFIX)) {
			loadPoints();
		}
		if (event.getGroup().equals(GroundMarkerConfig.GROUND_MARKER_CONFIG_GROUP)
			&& (event.getKey().equals(GroundMarkerConfig.SHOW_IMPORT_EXPORT_KEY_NAME)
				|| event.getKey().equals(GroundMarkerConfig.SHOW_CLEAR_KEY_NAME)))
		{
			// Maintain consistent menu option order by removing everything then adding according to config
//			sharingManager.removeMenuOptions();
//
//			if (config.showImportExport())
//			{
//				sharingManager.addImportExportMenuOptions();
//			}
//			if (config.showClear())
//			{
//				sharingManager.addClearMenuOption();
//			}
		}
	}

	private void markTile(LocalPoint localPoint)
	{
		if (localPoint == null)
		{
			return;
		}

		WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, localPoint);

		int regionId = worldPoint.getRegionID();
		GroundMarkerPoint point = new GroundMarkerPoint(regionId, worldPoint.getRegionX(), worldPoint.getRegionY(), client.getPlane(), config.markerColor(), null);
		log.debug("Updating point: {} - {}", point, worldPoint);

		List<GroundMarkerPoint> groundMarkerPoints = new ArrayList<>(getPoints(regionId));
		if (groundMarkerPoints.contains(point))
		{
			// since I don't save points this won't see any tiles added through ground markers immediately, hence the extra args.
			loadPoints(point, false);
		}
		else
		{
			// since I don't save points this won't see any tiles added through ground markers immediately, hence the extra args.
			loadPoints(point, true);
		}

//		savePoints(regionId, groundMarkerPoints);
	}

	private void labelTile(Tile tile)
	{
		LocalPoint localPoint = tile.getLocalLocation();
		WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, localPoint);
		final int regionId = worldPoint.getRegionID();

		GroundMarkerPoint searchPoint = new GroundMarkerPoint(regionId, worldPoint.getRegionX(), worldPoint.getRegionY(), client.getPlane(), null, null);
		Collection<GroundMarkerPoint> points = getPoints(regionId);
		GroundMarkerPoint existing = points.stream()
			.filter(p -> p.equals(searchPoint))
			.findFirst().orElse(null);
		if (existing == null)
		{
			return;
		}

		chatboxPanelManager.openTextInput("Tile label")
			.value(Optional.ofNullable(existing.getLabel()).orElse(""))
			.onDone((input) ->
			{
				input = Strings.emptyToNull(input);

				GroundMarkerPoint newPoint = new GroundMarkerPoint(regionId, worldPoint.getRegionX(), worldPoint.getRegionY(), client.getPlane(), existing.getColor(), input);
				points.remove(searchPoint);
				points.add(newPoint);
//				savePoints(regionId, points);

				loadPoints();
			})
			.build();
	}

	private Collection<GroundMarkerPoint> getBrushPoints(int regionId)
	{
		String json = configManager.getConfiguration(BRUSH_CONFIG_GROUP, REGION_PREFIX + regionId);
		if (Strings.isNullOrEmpty(json))
		{
			return Collections.emptyList();
		}

		// CHECKSTYLE:OFF
		return GSON.fromJson(json, new TypeToken<List<GroundMarkerPoint>>()
		{
		}.getType());
		// CHECKSTYLE:ON
	}
}
