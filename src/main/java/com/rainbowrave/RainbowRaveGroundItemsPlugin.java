/*
 * Copyright (c) 2017, Aria <aria@ar1as.space>
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import static com.rainbowrave.RainbowRaveConfig.GroundItemsToColor.HIGH;
import static com.rainbowrave.RainbowRaveConfig.GroundItemsToColor.INSANE;
import static com.rainbowrave.RainbowRaveConfig.GroundItemsToColor.LOW;
import static com.rainbowrave.RainbowRaveConfig.GroundItemsToColor.MEDIUM;
import java.awt.Color;
import java.awt.Rectangle;
import static java.lang.Boolean.TRUE;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.grounditems.GroundItemsConfig;
import net.runelite.client.plugins.grounditems.config.HighlightTier;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.RSTimeUnit;
import net.runelite.client.util.Text;

public class RainbowRaveGroundItemsPlugin
{

	public static final String GROUND_ITEMS_CONFIG_GROUP = "grounditems";
	public static final String SHOW_LOOTBEAM_TIER_CONFIG_KEY = "showLootbeamTier";
	public static final String SHOW_LOOTBEAM_FOR_HIGHLIGHTED_CONFIG_KEY = "showLootbeamForHighlighted";

	@Value
	static class PriceHighlight
	{
		private final int price;
		private final Color color;
	}

	// ItemID for coins
	private static final int COINS = ItemID.COINS_995;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private Map.Entry<Rectangle, GroundItem> textBoxBounds;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private Map.Entry<Rectangle, GroundItem> hiddenBoxBounds;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private Map.Entry<Rectangle, GroundItem> highlightBoxBounds;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private boolean hotKeyPressed;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private boolean hideAll;

	private List<String> hiddenItemList = new CopyOnWriteArrayList<>();
	private List<String> highlightedItemsList = new CopyOnWriteArrayList<>();

	@Inject
	public RainbowRaveGroundItemInputListener inputListener;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private GroundItemsConfig config;

//	@Inject
//	private RainbowRaveGroundItemsOverlay overlay;
//
	@Inject
	private Notifier notifier;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private RainbowRaveConfig rainbowRaveConfig;

	@Inject
	private RainbowRavePlugin rainbowRavePlugin;

	@Inject
	private ConfigManager configManager;

	@Getter
	private final Table<WorldPoint, Integer, GroundItem> collectedGroundItems = HashBasedTable.create();
	private List<PriceHighlight> priceChecks = ImmutableList.of();
	private LoadingCache<NamedQuantity, Boolean> highlightedItems;
	private LoadingCache<NamedQuantity, Boolean> hiddenItems;
	final Map<WorldPoint, Lootbeam> lootbeams = new HashMap<>();

//	@Override
	protected void startUp()
	{
		groundItemsLootBeamChange(false, false, true);
//		overlayManager.add(overlay);
		mouseManager.registerMouseListener(inputListener);
		keyManager.registerKeyListener(inputListener);
		executor.execute(this::reset);
	}

	@Subscribe
	public void onClientShutdown(ClientShutdown e) {
		if (rainbowRaveConfig.recolorLootBeams())
		{
			restoreGroundItemLootBeams();
		}
	}

//	@Override
	protected void shutDown()
	{
		if (rainbowRaveConfig.recolorLootBeams())
		{
			restoreGroundItemLootBeams();
		}
//		overlayManager.remove(overlay);
		mouseManager.unregisterMouseListener(inputListener);
		keyManager.unregisterKeyListener(inputListener);
		highlightedItems.invalidateAll();
		highlightedItems = null;
		hiddenItems.invalidateAll();
		hiddenItems = null;
		hiddenItemList = null;
		highlightedItemsList = null;
		collectedGroundItems.clear();
		clientThread.invokeLater(this::removeAllLootbeams);
	}

	private boolean ignoreConfigChange = false;

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (ignoreConfigChange) return;

		if (event.getGroup().equals(GROUND_ITEMS_CONFIG_GROUP))
		{
			if (event.getKey().equals(SHOW_LOOTBEAM_TIER_CONFIG_KEY)) {
				groundItemsLootBeamChange(true, false, false);
			}
			else if (event.getKey().equals(SHOW_LOOTBEAM_FOR_HIGHLIGHTED_CONFIG_KEY))
			{
				groundItemsLootBeamChange(false, true, false);
			}
			else
			{
				executor.execute(this::reset);
			}
		}
		else if (event.getGroup().equals(RainbowRavePlugin.GROUP))
		{
			if (event.getKey().equals(RainbowRaveConfig.RECOLOR_LOOT_BEAMS_KEY))
			{
				if (rainbowRaveConfig.recolorLootBeams())
				{
					groundItemsLootBeamChange(false, false, true);
				}
				else
				{
					restoreGroundItemLootBeams();
				}
			}
		}
	}

	private void groundItemsLootBeamChange(boolean tierChanged, boolean highlightedChanged, boolean turningOnRecolorLootBeams)
	{
		if (rainbowRaveConfig.recolorLootBeams())
		{
			if (!turningOnRecolorLootBeams && SwingUtilities.isEventDispatchThread()) // Changes from profile changes happen on the executor thread, only show the message when it happens on the swing thread.
			{
				clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
					"Rainbow Rave: Please change loot beam settings through the rainbow rave plugin's settings, as rainbow rave needs to disable regular loot beams in order to recolor them.",
					"", false));
			}
			ignoreConfigChange = true;
			if (tierChanged || turningOnRecolorLootBeams)
			{
				HighlightTier highlightTier = config.showLootbeamTier();
				rainbowRaveConfig.setGroundItemsLootbeamTier(highlightTier);
				configManager.setConfiguration(GROUND_ITEMS_CONFIG_GROUP, SHOW_LOOTBEAM_TIER_CONFIG_KEY, HighlightTier.OFF);
			}
			if (highlightedChanged || turningOnRecolorLootBeams)
			{
				boolean showLootbeamForHighlighted = config.showLootbeamForHighlighted();
				rainbowRaveConfig.setGroundItemsHighlightedItemsLootbeam(showLootbeamForHighlighted);
				configManager.setConfiguration(GROUND_ITEMS_CONFIG_GROUP, SHOW_LOOTBEAM_FOR_HIGHLIGHTED_CONFIG_KEY, false);
			}
			ignoreConfigChange = false;

			executor.execute(this::reset);
		}
	}

	private void restoreGroundItemLootBeams()
	{
		HighlightTier tier = rainbowRaveConfig.getGroundItemsLootbeamTier();
		Boolean showLootbeamForHighlighted = rainbowRaveConfig.getGroundItemsHighlightedItemsLootbeam();
		ignoreConfigChange = true;
		configManager.setConfiguration(GROUND_ITEMS_CONFIG_GROUP, SHOW_LOOTBEAM_TIER_CONFIG_KEY, tier);
		configManager.setConfiguration(GROUND_ITEMS_CONFIG_GROUP, SHOW_LOOTBEAM_FOR_HIGHLIGHTED_CONFIG_KEY, showLootbeamForHighlighted);
		ignoreConfigChange = false;

		executor.execute(this::reset);
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			collectedGroundItems.clear();
			lootbeams.clear();
		}
	}

	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		TileItem item = itemSpawned.getItem();
		Tile tile = itemSpawned.getTile();

		GroundItem groundItem = buildGroundItem(tile, item);
		GroundItem existing = collectedGroundItems.get(tile.getWorldLocation(), item.getId());
		if (existing != null)
		{
			existing.setQuantity(existing.getQuantity() + groundItem.getQuantity());
			// The spawn time remains set at the oldest spawn
		}
		else
		{
			collectedGroundItems.put(tile.getWorldLocation(), item.getId(), groundItem);
		}

//		if (!config.onlyShowOwnItems())
//		{
//			notifyHighlightedItem(groundItem);
//		}

		handleLootbeam(tile.getWorldLocation());
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		TileItem item = itemDespawned.getItem();
		Tile tile = itemDespawned.getTile();

		GroundItem groundItem = collectedGroundItems.get(tile.getWorldLocation(), item.getId());
		if (groundItem == null)
		{
			return;
		}

		if (groundItem.getQuantity() <= item.getQuantity())
		{
			collectedGroundItems.remove(tile.getWorldLocation(), item.getId());
		}
		else
		{
			groundItem.setQuantity(groundItem.getQuantity() - item.getQuantity());
			// When picking up an item when multiple stacks appear on the ground,
			// it is not known which item is picked up, so we invalidate the spawn
			// time
			groundItem.setSpawnTime(null);
		}

		handleLootbeam(tile.getWorldLocation());
	}

	@Subscribe
	public void onItemQuantityChanged(ItemQuantityChanged itemQuantityChanged)
	{
		TileItem item = itemQuantityChanged.getItem();
		Tile tile = itemQuantityChanged.getTile();
		int oldQuantity = itemQuantityChanged.getOldQuantity();
		int newQuantity = itemQuantityChanged.getNewQuantity();

		int diff = newQuantity - oldQuantity;
		GroundItem groundItem = collectedGroundItems.get(tile.getWorldLocation(), item.getId());
		if (groundItem != null)
		{
			groundItem.setQuantity(groundItem.getQuantity() + diff);
		}

		handleLootbeam(tile.getWorldLocation());
	}

//	@Subscribe
//	public void onClientTick(ClientTick event)
//	{
//	}

	private GroundItem buildGroundItem(final Tile tile, final TileItem item)
	{
		// Collect the data for the item
		final int itemId = item.getId();
		final ItemComposition itemComposition = itemManager.getItemComposition(itemId);
		final int realItemId = itemComposition.getNote() != -1 ? itemComposition.getLinkedNoteId() : itemId;
		final int alchPrice = itemComposition.getHaPrice();
		final int despawnTime = item.getDespawnTime() - client.getTickCount();
		final int visibleTime = item.getVisibleTime() - client.getTickCount();

		final GroundItem groundItem = GroundItem.builder()
			.id(itemId)
			.location(tile.getWorldLocation())
			.itemId(realItemId)
			.quantity(item.getQuantity())
			.name(itemComposition.getName())
			.haPrice(alchPrice)
			.height(tile.getItemLayer().getHeight())
			.tradeable(itemComposition.isTradeable())
			.ownership(item.getOwnership())
			.isPrivate(item.isPrivate())
			.spawnTime(Instant.now())
			.stackable(itemComposition.isStackable())
			.despawnTime(Duration.of(despawnTime, RSTimeUnit.GAME_TICKS))
			.visibleTime(Duration.of(visibleTime, RSTimeUnit.GAME_TICKS))
			.build();

		// Update item price in case it is coins
		if (realItemId == COINS)
		{
			groundItem.setHaPrice(1);
			groundItem.setGePrice(1);
		}
		else
		{
			groundItem.setGePrice(itemManager.getItemPrice(realItemId));
		}

		return groundItem;
	}

	private void reset()
	{
		// gets the hidden items from the text box in the config
		hiddenItemList = Text.fromCSV(config.getHiddenItems());

		// gets the highlighted items from the text box in the config
		highlightedItemsList = Text.fromCSV(config.getHighlightItems());

		highlightedItems = CacheBuilder.newBuilder()
			.maximumSize(512L)
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new WildcardMatchLoader(highlightedItemsList));

		hiddenItems = CacheBuilder.newBuilder()
			.maximumSize(512L)
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build(new WildcardMatchLoader(hiddenItemList));

		// Cache colors
		ImmutableList.Builder<PriceHighlight> priceCheckBuilder = ImmutableList.builder();

		if (config.insaneValuePrice() > 0)
		{
			priceCheckBuilder.add(new PriceHighlight(config.insaneValuePrice(), config.insaneValueColor()));
		}

		if (config.highValuePrice() > 0)
		{
			priceCheckBuilder.add(new PriceHighlight(config.highValuePrice(), config.highValueColor()));
		}

		if (config.mediumValuePrice() > 0)
		{
			priceCheckBuilder.add(new PriceHighlight(config.mediumValuePrice(), config.mediumValueColor()));
		}

		if (config.lowValuePrice() > 0)
		{
			priceCheckBuilder.add(new PriceHighlight(config.lowValuePrice(), config.lowValueColor()));
		}

		priceChecks = priceCheckBuilder.build();

		clientThread.invokeLater(this::handleLootbeams);
	}

//	@Subscribe
//	public void onMenuEntryAdded(MenuEntryAdded event)
//	{
//	}

//	void updateList(String item, boolean hiddenList)
//	{
//	}

	Optional<Color> getHighlighted(NamedQuantity item, int gePrice, int haPrice)
	{
		if (TRUE.equals(highlightedItems.getUnchecked(item)))
		{
			return rainbowRaveConfig.colorHighlightedGroundItems() ? Optional.of(rainbowRavePlugin.getColor(item.getName().hashCode())) : Optional.empty();
		}

		// Explicit hide takes priority over implicit highlight
		if (TRUE.equals(hiddenItems.getUnchecked(item)))
		{
			return null;
		}

		final int price = getValueByMode(gePrice, haPrice);
		if (price > config.insaneValuePrice())
		{
			return colorForTier(INSANE, item);
		}

		if (price > config.highValuePrice())
		{
			return colorForTier(HIGH, item);
		}

		if (price > config.mediumValuePrice())
		{
			return colorForTier(MEDIUM, item);
		}

		if (price > config.lowValuePrice())
		{
			return colorForTier(LOW, item);
		}

		return null;
	}

	private Optional<Color> colorForTier(RainbowRaveConfig.GroundItemsToColor tier, NamedQuantity item)
	{
		return rainbowRaveConfig.whichGroundItemsToColor().compareTo(tier) >= 0 ? Optional.of(rainbowRavePlugin.getColor(item.getName().hashCode())) : Optional.empty();
	}

	Optional<Color> getHidden(NamedQuantity item, int gePrice, int haPrice, boolean isTradeable)
	{
		final boolean isExplicitHidden = TRUE.equals(hiddenItems.getUnchecked(item));
		final boolean isExplicitHighlight = TRUE.equals(highlightedItems.getUnchecked(item));
		final boolean canBeHidden = gePrice > 0 || isTradeable || !config.dontHideUntradeables();
		final boolean underGe = gePrice < config.getHideUnderValue();
		final boolean underHa = haPrice < config.getHideUnderValue();

		// Explicit highlight takes priority over implicit hide
		return isExplicitHidden || (!isExplicitHighlight && canBeHidden && underGe && underHa)
			? rainbowRaveConfig.whichGroundItemsToColor().compareTo(RainbowRaveConfig.GroundItemsToColor.HIDDEN) >= 0 ? Optional.of(rainbowRavePlugin.getColor(item.getName().hashCode())) : Optional.empty()
			: null;
	}

	Optional<Color> getItemColor(Optional<Color> highlighted, Optional<Color> hidden, NamedQuantity item)
	{
		if (highlighted != null)
		{
			return highlighted;
		}

		if (hidden != null)
		{
			return hidden;
		}

		return rainbowRaveConfig.whichGroundItemsToColor().compareTo(RainbowRaveConfig.GroundItemsToColor.REGULAR) >= 0 ? Optional.of(rainbowRavePlugin.getColor(item.getName().hashCode())) : Optional.empty();
	}

	@Subscribe
	public void onFocusChanged(FocusChanged focusChanged)
	{
		if (!focusChanged.isFocused())
		{
			setHotKeyPressed(false);
		}
	}

//	private void notifyHighlightedItem(net.runelite.client.plugins.grounditems.GroundItem item)
//	{
//	}

	private int getValueByMode(int gePrice, int haPrice)
	{
		switch (config.valueCalculationMode())
		{
			case GE:
				return gePrice;
			case HA:
				return haPrice;
			default: // Highest
				return Math.max(gePrice, haPrice);
		}
	}

	private void handleLootbeam(WorldPoint worldPoint)
	{
		/*
		 * Return and remove the lootbeam from this location if lootbeam are disabled
		 * Lootbeam can be at this location if the config was just changed
		 */
		if (!rainbowRaveConfig.recolorLootBeams() || !(rainbowRaveConfig.getGroundItemsHighlightedItemsLootbeam() || rainbowRaveConfig.getGroundItemsLootbeamTier() != HighlightTier.OFF))
		{
			removeLootbeam(worldPoint);
			return;
		}

		int price = -1;
		Collection<GroundItem> groundItems = collectedGroundItems.row(worldPoint).values();
		for (GroundItem groundItem : groundItems)
		{
			if ((config.onlyShowOwnItems() && !groundItem.isMine()))
			{
				continue;
			}

			/*
			 * highlighted items have the highest priority so if an item is highlighted at this location
			 * we can early return
			 */
			NamedQuantity item = new NamedQuantity(groundItem);
			if (rainbowRaveConfig.getGroundItemsHighlightedItemsLootbeam()
				&& TRUE.equals(highlightedItems.getUnchecked(item)))
			{
				addLootbeam(worldPoint, config.highlightedColor(), null);
				return;
			}

			// Explicit hide takes priority over implicit highlight
			if (TRUE.equals(hiddenItems.getUnchecked(item)))
			{
				continue;
			}

			int itemPrice = getValueByMode(groundItem.getGePrice(), groundItem.getHaPrice());
			price = Math.max(itemPrice, price);
		}

		if (rainbowRaveConfig.getGroundItemsLootbeamTier() != HighlightTier.OFF)
		{
			for (PriceHighlight highlight : priceChecks)
			{
				if (price > highlight.getPrice() && price > rainbowRaveConfig.getGroundItemsLootbeamTier().getValueFromTier(config))
				{
					addLootbeam(worldPoint, highlight.color, rainbowRaveConfig.getGroundItemsLootbeamTier());
					return;
				}
			}
		}

		removeLootbeam(worldPoint);
	}

	private void handleLootbeams()
	{
		for (WorldPoint worldPoint : collectedGroundItems.rowKeySet())
		{
			handleLootbeam(worldPoint);
		}
	}

	private void removeAllLootbeams()
	{
		for (Lootbeam lootbeam : lootbeams.values())
		{
			lootbeam.remove();
		}

		lootbeams.clear();
	}

	private void addLootbeam(WorldPoint worldPoint, Color color, HighlightTier tier)
	{
		Lootbeam lootbeam = lootbeams.get(worldPoint);
		if (lootbeam == null)
		{
			lootbeam = new Lootbeam(client, clientThread, worldPoint, color, config.lootbeamStyle().name(), tier);
			lootbeams.put(worldPoint, lootbeam);
		}
		else
		{
			lootbeam.setColor(color);
			lootbeam.setStyle(config.lootbeamStyle().name());
		}
	}

	private void removeLootbeam(WorldPoint worldPoint)
	{
		Lootbeam lootbeam = lootbeams.remove(worldPoint);
		if (lootbeam != null)
		{
			lootbeam.remove();
		}
	}
}
