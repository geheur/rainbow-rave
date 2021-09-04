package com.rainbowrave;

//import lombok.Builder;

public class ConfigGenerator
{
//	private static final boolean autoPosition = true;
//
//	private static int i = 0;
//
//	public static void main(String[] args)
//	{
//		System.out.println(
//			SingleConfigGenerator.builder()
//				.key("colorSpeed")
//				.name("Color speed (ms)")
//				.description("How fast the colors change (ms per full cycle)")
//				.defaultValue(6000)
//				.typeName("int")
//				.build().generate()
//		);
//
//		System.out.println(
//		SingleConfigGenerator.builder()
//			.key("syncColor")
//			.name("Sync colors")
//			.description("Make all highlighted things be the same color as each other.")
//			.defaultValue(false)
//			.typeName("boolean")
//			.build().generate()
//		);
//
//		System.out.println(
//		SingleConfigGenerator.builder()
//			.key("highlightSelf")
//			.name("Highlight self")
//			.description("Highlight your own player character. Uses Npc Indicator's settings.")
//			.defaultValue(false)
//			.typeName("boolean")
//			.build().generate()
//		);
//
//		System.out.println(
//		SingleConfigGenerator.builder()
//			.key("highlightOthers")
//			.name("Highlight others")
//			.description("Highlight other players' characters. Uses Npc Indicator's settings.")
//			.defaultValue(false)
//			.typeName("boolean")
//			.build().generate()
//		);
//
//		System.out.println(
//		SingleConfigGenerator.builder()
//			.key("whichNpcsToHighlight")
//			.name("Npc highlight")
//			.description("Which npcs to highlight")
//			.defaultValue(RainbowRaveConfig.NpcsToHighlight.SAME)
//			.typeName("NpcsToHighlight")
//			.build().generate()
//		);
//
//		System.out.println(
//			SingleConfigGenerator.builder()
//				.key("smoothWaves")
//				.name("Tile color waves")
//				.description("Whether the tiles should have a smooth transition from color to color between two adjacent tiles.")
//				.defaultValue(false)
//				.typeName("boolean")
//				.build().generate()
//		);
//
//		System.out.println(
//			SingleConfigGenerator.builder()
//				.key("fillTiles")
//				.name("Fill tiles")
//				.description("Fills the tile with an opaque square.")
//				.defaultValue(false)
//				.typeName("boolean")
//				.build().generate()
//		);
//
//		System.out.println(
//			SingleConfigGenerator.builder()
//				.key("useBrushMarkerTiles")
//				.name("Brush Marker Tiles")
//				.description("Applies rainbow to tile markers from the \\\"Brush Markers\\\" plugin hub plugin. This plugin can be used to mark lots of tiles at once.")
//				.defaultValue(false)
//				.typeName("boolean")
//				.build().generate()
//		);
//
//		System.out.println(
//			SingleConfigGenerator.builder()
//				.key("whichObjectsToHighlight")
//				.name("Object highlight")
//				.description("Which objects to highlight.")
//				.defaultValue(RainbowRaveConfig.ObjectsToHighlight.SAME)
//				.typeName("ObjectsToHighlight")
//				.build().generate()
//		);
//
//		System.out.println(
//			SingleConfigGenerator.builder()
//				.key("whichItemsToInventoryTag")
//				.name("Inventory tags")
//				.description("Which items to tag.")
//				.defaultValue(RainbowRaveConfig.ItemsToTag.SAME)
//				.typeName("ItemsToTag")
//				.build().generate()
//		);
//
//		System.out.println(
//			SingleConfigGenerator.builder()
//				.key("colorHighlightedGroundItems")
//				.name("Highlight Ground Items")
//				.description("Applies rainbow to highlighted items.")
//				.defaultValue(true)
//				.typeName("boolean")
//				.build().generate()
//		);
//
//		System.out.println(
//			SingleConfigGenerator.builder()
//				.key("whichGroundItemsToColor")
//				.name("Ground Items")
//				.description("Items in this tier and above are colored.")
//				.defaultValue(RainbowRaveConfig.GroundItemsToColor.REGULAR)
//				.typeName("GroundItemsToColor")
//				.build().generate()
//		);
//
//		System.out.println(
//			SingleConfigGenerator.builder()
//				.key("recolorScytheSwings")
//				.name("Scythe swings")
//				.description("Recolor scythe swings. Swing trails may not return to normal after disabling.")
//				.defaultValue(false)
//				.typeName("boolean")
//				.build().generate()
//		);
//
//	}
//
//	@Builder
//	public static final class SingleConfigGenerator<T> {
//		private final String key;
//		private final String name;
//		private String description;
//		private String section;
//		private T defaultValue;
//		private String typeName;
//
//		public String generate() {
//			String n = "\n";
//			return String.format(
//					"\t@ConfigItem(" + n +
//					"\t\tkeyName = \"%s\"," + n +
//					"\t\tname = \"%s\"," + n +
//					"\t\tdescription = \"%s\"," + n +
//					(section == null ? "%s" : "\t\tsection = %s," + n) +
//					"\t\tposition = " + i++ + "" + n +
//					"\t)" + n +
//					"\tdefault " + ((typeName == null) ? defaultValue.getClass().getSimpleName() : typeName) + " %s()" + n +
//					"\t{" + n +
//					"\t\treturn %s;" + n +
//					"\t}" + n,
//				key, name, description, (section == null) ? "" : section, key, defaultValue
//			);
//		}
//	}
}
