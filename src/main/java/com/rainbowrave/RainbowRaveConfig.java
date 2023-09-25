package com.rainbowrave;

import static com.rainbowrave.RainbowRaveConfig.NpcsToHighlight.SAME;
import lombok.RequiredArgsConstructor;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.grounditems.config.HighlightTier;

@ConfigGroup(RainbowRavePlugin.GROUP)
public interface RainbowRaveConfig extends Config
{

	String LOOT_BEAMS_TIER_CONFIG_KEY = "groundItemsLootbeamTier";
	String LOOT_BEAMS_HIGHLIGHT_CONFIG_KEY = "groundItemsHighlightedItemsLootbeam";

	enum NpcsToHighlight {
		NONE,
		SAME,
		ALL
	}

	enum ObjectsToHighlight {
		NONE,
		SAME,
		ALL
	}

	enum ItemsToTag {
		NONE,
		SAME,
		ALL
	}

	@RequiredArgsConstructor
	enum GroundItemsToColor {
		NONE(HighlightTier.INSANE.ordinal() + 1),
		INSANE(HighlightTier.INSANE.ordinal()),
		HIGH(HighlightTier.HIGH.ordinal()),
		MEDIUM(HighlightTier.MEDIUM.ordinal()),
		LOW(HighlightTier.LOW.ordinal()),
		REGULAR(HighlightTier.LOW.ordinal() - 1),
		HIDDEN(HighlightTier.LOW.ordinal() - 2),
		;

		public final int highlightTierRelativeOrdinal;
	}

	enum GroundMarkerColorMode {
		WAVES,
		RANDOM,
		COLOR_SYNC
	}

	enum MouseTrailStyle {
		NONE,
		ENABLED,
		SYNCED,
		PARTYMODE,
	}

	@ConfigItem(
			keyName = "theme",
			name = "Theme",
			description = "The color theme used for highlighting things.",
			position = 0
	)
	default Theme theme()
	{
		return Theme.RAINBOW;
	}

	@ConfigItem(
		keyName = "colorSpeed",
		name = "Color speed (ms)",
		description = "How fast the colors change (ms per full cycle)",
		position = 0
	)
	default int colorSpeed()
	{
		return 6000;
	}

	@ConfigItem(
		keyName = "syncColor",
		name = "Sync colors",
		description = "Make all highlighted things be the same color as each other.",
		position = 1
	)
	default boolean syncColor()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightSelf",
		name = "Highlight self",
		description = "Highlight your own player character. Uses Npc Indicator's settings.",
		position = 2
	)
	default boolean highlightSelf()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightOthers",
		name = "Highlight others",
		description = "Highlight other players' characters. Uses Npc Indicator's settings.",
		position = 3
	)
	default boolean highlightOthers()
	{
		return false;
	}

	@ConfigItem(
		keyName = "whichNpcsToHighlight",
		name = "Npc highlight",
		description = "Which npcs to highlight",
		position = 4
	)
	default NpcsToHighlight whichNpcsToHighlight()
	{
		return SAME;
	}

	@ConfigItem(
		keyName = "rainbowTileMarkers",
		name = "Rainbow tile markers",
		description = "Make tile markers rainbow",
		position = 5
	)
	default boolean rainbowTileMarkers()
	{
		return true;
	}

	String OLD_TILE_COLOR_WAVES_KEY = "smoothWaves";
	String GROUND_MARKER_COLOR_MODE_KEY = "groundMarkerColorMode";

	@ConfigItem(name=
		"Tile Color Mode",
		description = "\"waves\" makes tile color smooth between adjacent tiles, and \"color sync\" makes tiles with the same color in ground markers share the same color.", keyName = GROUND_MARKER_COLOR_MODE_KEY, position = 6
	) default GroundMarkerColorMode groundMarkerColorMode() { return GroundMarkerColorMode.RANDOM; }

	@ConfigItem(name=
		"Fill tiles",
		description = "Fills the tile with an opaque square.", keyName = "fillTiles", position = 7
	) default boolean fillTiles() { return false; }

	@ConfigItem(name=
		"Brush Marker Tiles",
		description = "Applies rainbow to tile markers from the \"Brush Markers\" plugin hub plugin. This plugin can be used to mark lots of tiles at once.", keyName = "useBrushMarkerTiles", position = 8
	) default boolean useBrushMarkerTiles() { return false; }

	@ConfigItem(name=
		"Object highlight",
		description = "Which objects to highlight.", keyName = "whichObjectsToHighlight", position = 9
	) default ObjectsToHighlight whichObjectsToHighlight() { return ObjectsToHighlight.SAME; }

	@ConfigItem(name=
		"Inventory tags",
		description = "Which items to tag.", keyName = "whichItemsToInventoryTag", position = 10
	) default ItemsToTag whichItemsToInventoryTag() { return ItemsToTag.SAME; }

	@ConfigItem(name =
		"Highlight Ground Items",
		description = "Applies rainbow to highlighted items.", keyName = "colorHighlightedGroundItems", position = 11
	) default boolean colorHighlightedGroundItems() { return true; }

	@ConfigItem(name =
		"Ground Items",
		description = "Items in this tier and above are colored.", keyName = "whichGroundItemsToColor", position = 12
	) default GroundItemsToColor whichGroundItemsToColor() { return GroundItemsToColor.REGULAR; }

	@ConfigItem(name =
		"Scythe swings",
		description = "Recolor scythe swings. Swing trails may not return to normal after disabling.", keyName = "recolorScytheSwings", position = 13
	) default boolean recolorScytheSwings() { return false; }

	String RECOLOR_LOOT_BEAMS_KEY = "recolorLootBeams";
	@ConfigItem(
		keyName = RECOLOR_LOOT_BEAMS_KEY,
		name = "Loot beams",
		description = "Recolor loot beams.",
		position = 14
	)
	default boolean recolorLootBeams()
	{
		return true;
	}

	@ConfigItem(
			keyName = "whichMouseTrailStyle",
			name = "Mouse Trail Style",
			description = "Which trail style to use.",
			position = 16
	)
	default MouseTrailStyle whichMouseTrailStyle()
	{
		return MouseTrailStyle.NONE;
	}

	@ConfigItem(
		keyName = LOOT_BEAMS_TIER_CONFIG_KEY,
		name = "Lootbeam tier",
		description = "The lowest tier of which lootbeams should be shown for. You should modify this setting instead of the identical setting in Ground items, if you are using rainbow rave to recolor loot beams.",
		position = 101
	)
	default HighlightTier getGroundItemsLootbeamTier() {
		return HighlightTier.OFF; // Needs a value to prevent NPE in some situations.
	}

	@ConfigItem(
		keyName = LOOT_BEAMS_TIER_CONFIG_KEY,
		name = "",
		description = "",
		hidden = true
	)
	default void setGroundItemsLootbeamTier(HighlightTier tier) {

	}

	@ConfigItem(
		keyName = LOOT_BEAMS_HIGHLIGHT_CONFIG_KEY,
		name = "Highlighted item lootbeams",
		description = "Show lootbeams for highlighted items. You should modify this setting instead of the identical setting in Ground items, if you are using rainbow rave to recolor loot beams.",
		position = 100
	)
	default boolean getGroundItemsHighlightedItemsLootbeam() {
		return false;
	}

	@ConfigItem(
		keyName = LOOT_BEAMS_HIGHLIGHT_CONFIG_KEY,
		name = "",
		description = "",
		hidden = true
	)
	default void setGroundItemsHighlightedItemsLootbeam(boolean lootbeamsForHighlightedItems) {

	}

	@ConfigItem(
		keyName = "recolorDukeLights",
		name = "Duke rave",
		description = "Recolor the lights at duke. Duke lights may not return to normal after disabling.",
		position = 110
	)
	default boolean dukeRave()
	{
		return false;
	}

	@ConfigSection(name = "Tile Indicators", description = "Tile Indicators", position = 150)
	String tileIndicatorsSection = "tileIndicatorsSection";

	@ConfigItem(
		keyName = "recolorTileIndicators",
		name = "Tile indicators",
		description = "Recolor the tiles from the tile indicators plugin.",
		section = tileIndicatorsSection,
		position = 150
	)
	default boolean recolorTileIndicators() {
		return false;
	}

	@ConfigItem(
		keyName = "recolorTileIndicatorFill",
		name = "Also recolor fill",
		description = "Recolor the fill color of tile indicators as well",
		section = tileIndicatorsSection,
		position = 151
	)
	default boolean recolorTileIndicatorFill() {
		return false;
	}

	@ConfigItem(
		keyName = "preferCornerTileIndicatorsConfig",
		name = "Prefer \"Corner tile indicators\"",
		description = "Prefers to use the config from my \"Corner tile indicators\" plugin, if it is installed, including true tile fadeout and corner-only highlight.",
		section = tileIndicatorsSection,
		position = 152
	)
	default boolean preferCornerTileIndicatorsConfig() {
		return false;
	}
}
