# Rainbow Rave

Rainbow Rave is a [RuneLite Plugin Hub](https://runelite.net/plugin-hub/) plugin that adds animated colors to highlights and effects in Old School RuneScape. It can recolor ground items and loot beams, tile markers and tile indicators, NPCs and players, objects, inventory tags, scythe swing trails, and more.

## Install and configure

1. Open RuneLite's **Plugin Hub** and search for **Rainbow Rave**.
2. Install and enable the plugin.
3. Open its settings in RuneLite's plugin list. Choose a **Theme** and adjust **Color speed** or **Sync colors** to taste.
4. Enable the effects you want. For example, turn on **Scythe swings** for swing trails, or **Tile indicators** to recolor RuneLite's tile indicators.

Some effects use settings from RuneLite's corresponding built-in plugins, such as Ground Items, Ground Markers, NPC Indicators, Object Indicators, or Inventory Tags. If an effect is missing, check both Rainbow Rave's settings and the relevant built-in plugin's settings. Loot-beam options are configured in Rainbow Rave when using its recolored loot beams.

**Note:** Scythe swing trails and Duke lights may not immediately return to their original colors after you disable their respective options.

## Build and test from source

Use **JDK 11** for this repository's Gradle 6.6.1 wrapper. From the repository root:

```sh
# macOS/Linux
bash ./gradlew clean test
```

```powershell
# Windows PowerShell
.\gradlew.bat clean test
```

To try the development client, open the project as a Gradle project in your Java IDE and run `src/test/java/com/rainbowrave/ExamplePluginTest.java` as a Java application. Enable assertions (`-ea`) in the run configuration's VM options. This loads Rainbow Rave into a RuneLite development client; the automated tests alone do not verify in-game effects.

## Issues

Report problems and suggestions in [GitHub Issues](https://github.com/geheur/rainbow-rave/issues). Include your RuneLite version, what you expected, what happened, and any relevant client log errors. Do not share account credentials or credential files.

## License

See [LICENSE](LICENSE).
