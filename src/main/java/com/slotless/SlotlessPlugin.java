package com.slotless;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

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
		client.runScript(Objects.requireNonNull(client.getWidget(ComponentID.INVENTORY_CONTAINER)).getOnInvTransmitListener());
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
		}
	}

	private void replaceInventory() {
		int filler = config.inventoryReplacementItemId();
		Widget w = client.getWidget(ComponentID.INVENTORY_CONTAINER);
		if (w == null) {
			return;
		}
		for (Widget i : w.getDynamicChildren())
		{
			if (i.getItemId() == filler)
			{
				log.debug("Replacing {} with item filler", i.getName());
				i.setName("Filler");
				i.setTargetVerb(null);
				i.setItemId(ItemID.BANK_FILLER);
				i.setClickMask(0);
				i.setOnDragCompleteListener((Object[]) null);
				i.setOnDragListener((Object[]) null);
				Arrays.fill(Objects.requireNonNull(i.getActions()), "");
			} else if (i.getActions() != null)
			{
				final ItemStats itemStats = itemManager.getItemStats(i.getItemId(), false);
				if (itemStats != null && itemStats.isEquipable()) {
					final ItemEquipmentStats equipmentStats = itemStats.getEquipment();
					final int slot = equipmentStats.getSlot();
					if (equipmentStats.isTwoHanded()) {
						if (!config.weaponUnlocked() || !config.shieldUnlocked()) {
							removeEquipOption(i);
						}
						continue;
					}

					if (!checkSlot(slot)) {
						removeEquipOption(i);
					}
				}
			}
		}
	}

	private boolean checkSlot(int equipmentSlot) {
		switch(equipmentSlot) {
			case 0:
				return config.headUnlocked();
			case 1:
				return config.capeUnlocked();
			case 2:
				return config.amuletUnlocked();
			case 3:
				return config.weaponUnlocked();
			case 4:
				return config.bodyUnlocked();
			case 5:
				return config.shieldUnlocked();
			case 6:
				return config.legsUnlocked();
			case 7:
				return config.glovesUnlocked();
			case 8:
				return config.bootsUnlocked();
			case 9:
				return config.ringUnlocked();
			case 10:
				return config.ammoUnlocked();
			default:
				return false;
		}
	}

	private void removeEquipOption(Widget i) {
		if (i.getActions() == null) {
			return;
		}
		String[] actions = i.getActions();
		for (int actionIdx = 0; actionIdx < actions.length; ++actionIdx) {
			if ("Wear".equalsIgnoreCase(actions[actionIdx]) || "Wield".equalsIgnoreCase(actions[actionIdx]) || "Equip".equalsIgnoreCase(actions[actionIdx]))
			{
				actions[actionIdx] = "";
			}
		}
	}
}
