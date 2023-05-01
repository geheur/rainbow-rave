package com.rainbowrave;

import static com.rainbowrave.RainbowRaveConfig.NpcsToHighlight.SAME;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(RainbowRavePlugin.GROUP)
public interface RainbowRaveConfig extends Config
{
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

	enum GroundItemsToColor {
		NONE,
		INSANE,
		HIGH,
		MEDIUM,
		LOW,
		REGULAR,
		HIDDEN
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

	@ConfigItem(
		keyName = GROUND_MARKER_COLOR_MODE_KEY,
		name = "Tile Color Mode",
		description = "\"waves\" makes tile color smooth between adjacent tiles, and \"color sync\" makes tiles with the same color in ground markers share the same color.",
		position = 6
	)
	default GroundMarkerColorMode groundMarkerColorMode()
	{
		return GroundMarkerColorMode.RANDOM;
	}

	@ConfigItem(
		keyName = "fillTiles",
		name = "Fill tiles",
		description = "Fills the tile with an opaque square.",
		position = 7
	)
	default boolean fillTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "useBrushMarkerTiles",
		name = "Brush Marker Tiles",
		description = "Applies rainbow to tile markers from the \"Brush Markers\" plugin hub plugin. This plugin can be used to mark lots of tiles at once.",
		position = 8
	)
	default boolean useBrushMarkerTiles()
	{
		return false;
	}

	@ConfigItem(
		keyName = "whichObjectsToHighlight",
		name = "Object highlight",
		description = "Which objects to highlight.",
		position = 9
	)
	default ObjectsToHighlight whichObjectsToHighlight()
	{
		return ObjectsToHighlight.SAME;
	}

	@ConfigItem(
		keyName = "whichItemsToInventoryTag",
		name = "Inventory tags",
		description = "Which items to tag.",
		position = 10
	)
	default ItemsToTag whichItemsToInventoryTag()
	{
		return ItemsToTag.SAME;
	}

	@ConfigItem(
		keyName = "colorHighlightedGroundItems",
		name = "Highlight Ground Items",
		description = "Applies rainbow to highlighted items.",
		position = 11
	)
	default boolean colorHighlightedGroundItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "whichGroundItemsToColor",
		name = "Ground Items",
		description = "Items in this tier and above are colored.",
		position = 12
	)
	default GroundItemsToColor whichGroundItemsToColor()
	{
		return GroundItemsToColor.REGULAR;
	}

	@ConfigItem(
		keyName = "recolorScytheSwings",
		name = "Scythe swings",
		description = "Recolor scythe swings. Swing trails may not return to normal after disabling.",
		position = 13
	)
	default boolean recolorScytheSwings()
	{
		return false;
	}

	@ConfigItem(
		keyName = "recolorLootBeams",
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
			keyName = "mouseTrailLength",
			name = "Mouse Trail Length",
			description = "How long should the trail be.",
			position = 17
	)
	default int mouseTrailLength()
	{
		return 50;
	}
}
