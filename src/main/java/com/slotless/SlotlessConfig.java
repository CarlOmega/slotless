package com.slotless;

import net.runelite.api.ItemID;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("slotless")
public interface SlotlessConfig extends Config
{
	@ConfigItem(
			keyName = "inventoryReplacementItemId",
			name = "Inventory Item ID",
			description = "Item to be replaced with Bank Filler"
	)
	default int inventoryReplacementItemId()
	{
		return ItemID.AL_KHARID_FLYER;
	}
}
