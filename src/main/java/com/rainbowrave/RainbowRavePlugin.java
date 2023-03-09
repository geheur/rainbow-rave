/*
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

import com.google.inject.Provides;
import com.rainbowrave.RainbowRaveNpcIndicatorsPlugin.HighlightedNpc;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.events.ClientTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.grounditems.GroundItemsConfig;
import net.runelite.client.plugins.grounditems.GroundItemsPlugin;
import net.runelite.client.plugins.groundmarkers.GroundMarkerConfig;
import net.runelite.client.plugins.groundmarkers.GroundMarkerPlugin;
import net.runelite.client.plugins.inventorytags.InventoryTagsConfig;
import net.runelite.client.plugins.inventorytags.InventoryTagsPlugin;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsConfig;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsPlugin;
import net.runelite.client.plugins.objectindicators.ObjectIndicatorsConfig;
import net.runelite.client.plugins.objectindicators.ObjectIndicatorsPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Slf4j
@PluginDescriptor(
	name = "Rainbow Rave",
	tags = {"loot, beam"}
)
@PluginDependency(NpcIndicatorsPlugin.class)
@PluginDependency(GroundMarkerPlugin.class)
@PluginDependency(ObjectIndicatorsPlugin.class)
@PluginDependency(InventoryTagsPlugin.class)
@PluginDependency(GroundItemsPlugin.class)
public class RainbowRavePlugin extends Plugin
{
	public static final String GROUP = "rainbow_rave";

	@Inject
	private Client client;

	@Inject
	private RainbowRaveConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	private NpcIndicatorsConfig npcIndicatorsConfig;

	@Inject
	private RainbowRaveNpcIndicatorsPlugin rainbowRaveNpcIndicatorsPlugin;

	private RainbowRaveNpcSceneOverlay rainbowRaveNpcSceneOverlay;

	@Inject
	private GroundMarkerConfig groundMarkerConfig;

	@Inject
	private RainbowRaveGroundMarkerPlugin rainbowRaveGroundMarkerPlugin;

	private RainbowRaveGroundMarkerOverlay rainbowRaveGroundMarkerOverlay;

	@Inject
	private ObjectIndicatorsConfig objectIndicatorsConfig;

	@Inject
	private RainbowRaveObjectIndicatorsPlugin rainbowRaveObjectIndicatorsPlugin;

	private RainbowRaveObjectIndicatorsOverlay rainbowRaveObjectIndicatorsOverlay;

	private RainbowRaveInventoryTagsOverlay rainbowRaveInventoryTagsOverlay;

	@Inject
	private InventoryTagsConfig inventoryTagsConfig;

	@Inject
	private GroundItemsConfig groundItemsConfig;

	@Inject
	private RainbowRaveGroundItemsPlugin rainbowRaveGroundItemsPlugin;

	private RainbowRaveGroundItemsOverlay rainbowRaveGroundItemsOverlay;

	@Inject
	private RainbowRaveMouseTrailPlugin rainbowRaveMouseTrailPlugin;

	private RainbowRaveMouseTrailOverlay rainbowRaveMouseTrailOverlay;

	@Inject
	private EventBus eventBus;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@Override
	protected void startUp()
	{
		if (rainbowRaveGroundMarkerOverlay == null) {
			rainbowRaveGroundMarkerOverlay = new RainbowRaveGroundMarkerOverlay(client, groundMarkerConfig, rainbowRaveGroundMarkerPlugin, this, config);
		}
		rainbowRaveGroundMarkerPlugin.startUp();
		overlayManager.add(rainbowRaveGroundMarkerOverlay);
		eventBus.register(rainbowRaveGroundMarkerPlugin);

		if (rainbowRaveObjectIndicatorsOverlay == null) {
			rainbowRaveObjectIndicatorsOverlay = new RainbowRaveObjectIndicatorsOverlay(client, objectIndicatorsConfig, rainbowRaveObjectIndicatorsPlugin, modelOutlineRenderer, this, config);
		}
		rainbowRaveObjectIndicatorsPlugin.startUp();
		overlayManager.add(rainbowRaveObjectIndicatorsOverlay);
		eventBus.register(rainbowRaveObjectIndicatorsPlugin);

		if (rainbowRaveNpcSceneOverlay == null) {
			rainbowRaveNpcSceneOverlay = new RainbowRaveNpcSceneOverlay(client, npcIndicatorsConfig, rainbowRaveNpcIndicatorsPlugin, modelOutlineRenderer, this, config);
		}
		updateNpcHighlighterWithConfigSettings();
		rainbowRaveNpcIndicatorsPlugin.startUp();
		overlayManager.add(rainbowRaveNpcSceneOverlay);
		eventBus.register(rainbowRaveNpcIndicatorsPlugin);

		if (rainbowRaveInventoryTagsOverlay == null) {
			rainbowRaveInventoryTagsOverlay = new RainbowRaveInventoryTagsOverlay(itemManager, this, inventoryTagsConfig, config, configManager);
		}
		overlayManager.add(rainbowRaveInventoryTagsOverlay);

		if (rainbowRaveGroundItemsOverlay == null) {
			rainbowRaveGroundItemsOverlay = new RainbowRaveGroundItemsOverlay(client, rainbowRaveGroundItemsPlugin, groundItemsConfig);
		}
		overlayManager.add(rainbowRaveGroundItemsOverlay);
		rainbowRaveGroundItemsPlugin.startUp();
		eventBus.register(rainbowRaveGroundItemsPlugin);

		if (rainbowRaveMouseTrailOverlay == null) {
			rainbowRaveMouseTrailOverlay = new RainbowRaveMouseTrailOverlay(this, rainbowRaveMouseTrailPlugin, config);
		}
		overlayManager.add(rainbowRaveMouseTrailOverlay);
		rainbowRaveMouseTrailPlugin.startUp();
		eventBus.register(rainbowRaveMouseTrailPlugin);

		migrateConfig();
	}

	private void migrateConfig()
	{
		String previousSmoothWaves = configManager.getConfiguration(GROUP, RainbowRaveConfig.OLD_TILE_COLOR_WAVES_KEY);
		if (previousSmoothWaves != null) {
			if (Boolean.valueOf(previousSmoothWaves)) {
				configManager.setConfiguration(GROUP, RainbowRaveConfig.GROUND_MARKER_COLOR_MODE_KEY, RainbowRaveConfig.GroundMarkerColorMode.WAVES);
			} // else it would be "random" which is default
			configManager.unsetConfiguration(GROUP, RainbowRaveConfig.OLD_TILE_COLOR_WAVES_KEY);
		}
	}

	@Override
	protected void shutDown()
	{
		rainbowRaveGroundMarkerPlugin.shutDown();
		overlayManager.remove(rainbowRaveGroundMarkerOverlay);
		eventBus.unregister(rainbowRaveGroundMarkerPlugin);

		rainbowRaveObjectIndicatorsPlugin.shutDown();
		overlayManager.remove(rainbowRaveObjectIndicatorsOverlay);
		eventBus.unregister(rainbowRaveObjectIndicatorsPlugin);

		rainbowRaveNpcIndicatorsPlugin.shutDown();
		overlayManager.remove(rainbowRaveNpcSceneOverlay);
		eventBus.unregister(rainbowRaveNpcIndicatorsPlugin);

		overlayManager.remove(rainbowRaveInventoryTagsOverlay);

		overlayManager.remove(rainbowRaveGroundItemsOverlay);
		rainbowRaveGroundItemsPlugin.shutDown();
		eventBus.unregister(rainbowRaveGroundItemsPlugin);

		overlayManager.remove(rainbowRaveMouseTrailOverlay);
		rainbowRaveMouseTrailPlugin.shutDown();
		eventBus.unregister(rainbowRaveMouseTrailPlugin);
	}

	@Provides
	RainbowRaveConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RainbowRaveConfig.class);
	}

	private Function<NPC, HighlightedNpc> highlighterPredicate;

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		checkAndPushOverlayToFront(configChanged, "grounditemsplugin", rainbowRaveGroundItemsOverlay);
		checkAndPushOverlayToFront(configChanged, "groundmarkerplugin", rainbowRaveGroundMarkerOverlay);
		checkAndPushOverlayToFront(configChanged, "inventorytagsplugin", rainbowRaveInventoryTagsOverlay);
		checkAndPushOverlayToFront(configChanged, "npcindicatorsplugin", rainbowRaveNpcSceneOverlay);
		checkAndPushOverlayToFront(configChanged, "objectindicatorsplugin", rainbowRaveObjectIndicatorsOverlay);
		checkAndPushOverlayToFront(configChanged, "brushmarkerplugin", rainbowRaveGroundMarkerOverlay);

		if (configChanged.getGroup().equals("rainbow_rave") && configChanged.getKey().equals("whichNpcsToHighlight")) {
			updateNpcHighlighterWithConfigSettings();
		}
	}

	private void checkAndPushOverlayToFront(ConfigChanged configChanged, String key, Overlay overlay)
	{
		if (configChanged.getGroup().equals("runelite") && configChanged.getKey().equals(key) && configChanged.getNewValue().equalsIgnoreCase("true")) {
			clientThread.invokeLater(() -> {
				overlayManager.remove(overlay);
				overlayManager.add(overlay);
			});
		}
	}

	private void updateNpcHighlighterWithConfigSettings()
	{
		Function<NPC, HighlightedNpc> f;
		if (config.whichNpcsToHighlight() == RainbowRaveConfig.NpcsToHighlight.ALL) {
			f = npc -> rainbowRaveNpcIndicatorsPlugin.highlightedNpc(npc);
			rainbowRaveNpcSceneOverlay.enable(true);
		} else if (config.whichNpcsToHighlight() == RainbowRaveConfig.NpcsToHighlight.SAME) {
			f = npc -> null;
			rainbowRaveNpcSceneOverlay.enable(true);
		} else if (config.whichNpcsToHighlight() == RainbowRaveConfig.NpcsToHighlight.NONE) {
			f = npc -> null;
			rainbowRaveNpcSceneOverlay.enable(false);
		} else {
			return;
		}
		if (highlighterPredicate != null) rainbowRaveNpcIndicatorsPlugin.unregisterHighlighter(highlighterPredicate);
		highlighterPredicate = f;
		rainbowRaveNpcIndicatorsPlugin.registerHighlighter(highlighterPredicate);
		rainbowRaveNpcIndicatorsPlugin.rebuild();
	}

	public Color getColor(int hashCode)
	{
		return getColor(hashCode, client.getGameCycle(), config.syncColor(), config.colorSpeed());
	}

	public Color getColor(int hashCode, int gameCycle, boolean syncColor, int colorSpeed)
	{
		if (syncColor) hashCode = 0;
		int clientTicks = colorSpeed / 20;
		return config.theme().getColor(((hashCode + gameCycle) % clientTicks) / ((float) clientTicks));
	}

	private static final List<Integer> scytheTrailIds = Arrays.asList(478, 506, 1172);

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		for (GraphicsObject graphicsObject : client.getGraphicsObjects())
		{
			if (
				config.recolorScytheSwings() && (
					scytheTrailIds.contains(graphicsObject.getId()) ||
					(graphicsObject.getId() >= 1231 && graphicsObject.getId() <= 1235) || // chally trails + 1 red trail.
					(graphicsObject.getId() >= 1891 && graphicsObject.getId() <= 1898) // sara and sang scythe swing trails
				) ||
				config.recolorLootBeams() && graphicsObject instanceof RuneLiteObject
			)
			{
				recolorAllFaces(graphicsObject.getModel(), getColor(graphicsObject.getLocation().hashCode()));
			}
		}
	}

	private int colorToRs2hsb(Color color)
	{
		float[] hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

		// "Correct" the brightness level to avoid going to white at full saturation, or having a low brightness at
		// low saturation
		hsbVals[2] -= Math.min(hsbVals[1], hsbVals[2] / 2);

		int encode_hue = (int)(hsbVals[0] * 63);
		int encode_saturation = (int)(hsbVals[1] * 7);
		int encode_brightness = (int)(hsbVals[2] * 127);
		return (encode_hue << 10) + (encode_saturation << 7) + (encode_brightness);
	}

	private void recolorAllFaces(Model model, Color color)
	{
		if (model == null || color == null)
		{
			return;
		}

		int rs2hsb = colorToRs2hsb(color);
		int[] faceColors1 = model.getFaceColors1();
		int[] faceColors2 = model.getFaceColors2();
		int[] faceColors3 = model.getFaceColors3();

		for (int i = 0; i < faceColors1.length; i++)
		{
			faceColors1[i] = rs2hsb;
		}
		for (int i = 0; i < faceColors2.length; i++)
		{
			faceColors2[i] = rs2hsb;
		}
		for (int i = 0; i < faceColors3.length; i++)
		{
			faceColors3[i] = rs2hsb;
		}
	}
}
