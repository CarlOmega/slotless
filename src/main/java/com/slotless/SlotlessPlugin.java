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
		// Ensures equipment to be redrawn
		int slot = 0;
		for (int i = 0; i < 11; i++) {
			client.runScript(545, 25362447 + i, slot, 1,1,2);
			slot++;
			if (slot == 6 || slot == 8 ||slot == 11) {
				slot++;
			}
		}
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
		// for each equipment call (need to find the parent script but not sure if there is one)
		// might need to optimise this when
		if (id == 545) {
			replaceEquipment();
		}
	}

	private void replaceEquipment() {
		Widget w = client.getWidget(ComponentID.EQUIPMENT_INVENTORY_ITEM_CONTAINER);
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
				Arrays.fill(Objects.requireNonNull(i.getActions()), "");
			}
		}
	}

	private void replaceInventory() {
		int filler = config.inventoryReplacementItemId();
		Widget w = client.getWidget(ComponentID.INVENTORY_CONTAINER);
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
				i.setOnDragCompleteListener((Object[]) null);
				i.setOnDragListener((Object[]) null);
				Arrays.fill(Objects.requireNonNull(i.getActions()), "");
			} else if (i.getActions() != null)
			{
				String[] actions = i.getActions();
				for (int actionIdx = 0; actionIdx < actions.length; ++actionIdx) {
					if ("Wear".equalsIgnoreCase(actions[actionIdx]) || "Wield".equalsIgnoreCase(actions[actionIdx]) || "Equip".equalsIgnoreCase(actions[actionIdx]))
					{
						actions[actionIdx] = "";
					}
				}

			}
		}
	}
}
