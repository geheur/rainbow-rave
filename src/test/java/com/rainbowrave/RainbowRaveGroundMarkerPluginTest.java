package com.rainbowrave;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import net.runelite.api.Client;
import org.junit.Test;

public class RainbowRaveGroundMarkerPluginTest
{
	@Test
	public void loadPointsReturnsWhenWorldViewIsUnavailable() throws Exception
	{
		Client client = (Client) Proxy.newProxyInstance(
			Client.class.getClassLoader(),
			new Class<?>[]{Client.class},
			(proxy, method, args) -> null);

		RainbowRaveGroundMarkerPlugin plugin = new RainbowRaveGroundMarkerPlugin();
		setField(plugin, "client", client);

		plugin.loadPoints();
	}

	private static void setField(Object target, String name, Object value) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
