package com.rainbowrave;

import lombok.RequiredArgsConstructor;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

public class OtherPluginConfigManager
{
	@RequiredArgsConstructor
	public static final class ManagedConfig {
		private final String group;
		private final String key;
		private final Object desiredValue;
		private Object currentValue;
	}

	public void startUp() {

	}

	public void shutDown() {

	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
//		ManagedConfig managedConfig = getManagedChange(configChanged);
//		if (managedConfig == null) return;
//
//		if (!managedConfig.desiredValue.equals(managedConfig.getActualValue())) {
//			managedConfig.setValueToDesired();
//
//			// TODO nice message.
//		}
	}
}
