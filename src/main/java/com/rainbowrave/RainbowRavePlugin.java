package com.rainbowrave;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.function.Function;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.groundmarkers.GroundMarkerConfig;
import net.runelite.client.plugins.groundmarkers.GroundMarkerPlugin;
import net.runelite.client.plugins.npchighlight.HighlightedNpc;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsConfig;
import net.runelite.client.plugins.npchighlight.NpcIndicatorsPlugin;
import net.runelite.client.plugins.objectindicators.ObjectIndicatorsConfig;
import net.runelite.client.plugins.objectindicators.ObjectIndicatorsPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Slf4j
@PluginDescriptor(
	name = "Rainbow Rave"
)
@PluginDependency(NpcIndicatorsPlugin.class)
@PluginDependency(GroundMarkerPlugin.class)
@PluginDependency(ObjectIndicatorsPlugin.class)
public class RainbowRavePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RainbowRaveConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RainbowRaveOverlay rainbowRaveOverlay;

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

	@Inject
	private EventBus eventBus;

	@Inject
	private OtherPluginConfigManager otherPluginConfigManager;

	@Override
	protected void startUp()
	{
//		overlayManager.add(rainbowRaveOverlay);
//		eventBus.register(otherPluginConfigManager);

		if (rainbowRaveGroundMarkerOverlay == null) {
			rainbowRaveGroundMarkerOverlay = new RainbowRaveGroundMarkerOverlay(client, groundMarkerConfig, rainbowRaveGroundMarkerPlugin, this, config);
		}
		rainbowRaveGroundMarkerPlugin.startUp();
		overlayManager.add(rainbowRaveGroundMarkerOverlay);
		eventBus.register(rainbowRaveGroundMarkerPlugin);

		if (rainbowRaveObjectIndicatorsOverlay == null) {
			rainbowRaveObjectIndicatorsOverlay = new RainbowRaveObjectIndicatorsOverlay(client, objectIndicatorsConfig, rainbowRaveObjectIndicatorsPlugin, modelOutlineRenderer, config);
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
	}

	@Override
	protected void shutDown()
	{
//		overlayManager.remove(rainbowRaveOverlay);
//		eventBus.unregister(otherPluginConfigManager);

		rainbowRaveGroundMarkerPlugin.shutDown();
		overlayManager.remove(rainbowRaveGroundMarkerOverlay);
		eventBus.unregister(rainbowRaveGroundMarkerPlugin);

		rainbowRaveObjectIndicatorsPlugin.shutDown();
		overlayManager.remove(rainbowRaveObjectIndicatorsOverlay);
		eventBus.unregister(rainbowRaveObjectIndicatorsPlugin);

		rainbowRaveNpcIndicatorsPlugin.shutDown();
		overlayManager.remove(rainbowRaveNpcSceneOverlay);
		eventBus.unregister(rainbowRaveNpcIndicatorsPlugin);
	}

	@Provides
	RainbowRaveConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RainbowRaveConfig.class);
	}

	private Function<NPC, HighlightedNpc> highlighterPredicate;

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if (configChanged.getGroup().equals("rainbow_rave") && configChanged.getKey().equals("whichNpcsToHighlight")) {
			updateNpcHighlighterWithConfigSettings();
		}
	}

	// TODO just modify the rainbow rave npc indicators plugin? May be simpler.
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
		return RainbowRavePlugin.getColor(hashCode, client.getGameCycle(), config.syncColor(), config.colorSpeed());
	}

	public static Color getColor(int hashCode, int gameCycle, boolean syncColor, int colorSpeed)
	{
		if (syncColor) hashCode = 0;
		int clientTicks = colorSpeed / 20;
		return Color.getHSBColor(((hashCode + gameCycle) % clientTicks) / ((float) clientTicks), 1.0f, 1.0f);
	}
}
