package com.slotless;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
	name = "Slotless"
)
public class SlotlessPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;
	@Inject
	private SlotlessConfig config;

	@Inject
	private ItemManager itemManager;

	@Override
	protected void startUp()
	{
		clientThread.invoke(this::redrawInventory);
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(this::redrawInventory);
	}

	private void redrawInventory()
	{
		client.runScript(client.getWidget(WidgetInfo.INVENTORY).getOnInvTransmitListener());
		client.runScript(client.getWidget(WidgetInfo.EQUIPMENT).getOnInvTransmitListener());
	}

	@Provides
	SlotlessConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SlotlessConfig.class);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired) {
		final int id = scriptPostFired.getScriptId();

		if (id == ScriptID.INVENTORY_DRAWITEM - 1) {
			replaceInventory();
			replaceEquipment();
		}
	}

	private void replaceEquipment() {
		Widget w = client.getWidget(WidgetInfo.EQUIPMENT);
		if (w == null) {
			return;
		}
		log.debug("Replacing Equipment with item fillers");
		for (Widget i : w.getStaticChildren())
		{
			Widget child = i.getChild(1);
			if (child != null && child.getItemId() != 20594 && child.getItemId() > 0) {
				log.debug("Replacing Equipment: {}, id:{}, itemId:{}", i.getName(), i.getId(), child.getItemId());
				child.setName("Filler");
				child.setTargetVerb(null);
				child.setItemId(ItemID.BANK_FILLER);
				child.setClickMask(0);
				child.setOnDragCompleteListener((Object) null);
				child.setOnDragListener((Object) null);
				Arrays.fill(Objects.requireNonNull(i.getActions()), "");
			}
		}
	}

	private void replaceInventory() {
		int filler = config.inventoryReplacementItemId();
		Widget w = client.getWidget(WidgetInfo.INVENTORY);
		if (w == null) {
			return;
		}
		log.debug("Replacing Inventory with item fillers");
		for (Widget i : w.getDynamicChildren())
		{
			if (i.getItemId() == filler)
			{
				i.setName("Filler");
				i.setTargetVerb(null);
				i.setItemId(ItemID.BANK_FILLER);
				i.setClickMask(0);
				i.setOnDragCompleteListener(null);
				i.setOnDragListener(null);
				Arrays.fill(i.getActions(), "");
			}
		}
	}
}
