package com.slotless;

import net.runelite.api.EquipmentInventorySlot;
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

	@ConfigItem(keyName = "headUnlocked", name = "Unlock Head", description = "Unlock head slot")
	default boolean headUnlocked()
	{
		return false;
	}

	@ConfigItem(keyName = "capeUnlocked", name = "Unlock Cape", description = "Unlock cape slot")
	default boolean capeUnlocked()
	{
		return false;
	}
	@ConfigItem(keyName = "amuletUnlocked", name = "Unlock Amulet", description = "Unlock amulet slot")
	default boolean amuletUnlocked()
	{
		return false;
	}

	@ConfigItem(keyName = "weaponUnlocked", name = "Unlock Weapon", description = "Unlock weapon slot")
	default boolean weaponUnlocked()
	{
		return false;
	}

	@ConfigItem(keyName = "bodyUnlocked", name = "Unlock Body", description = "Unlock body slot")
	default boolean bodyUnlocked()
	{
		return false;
	}

	@ConfigItem(keyName = "shieldUnlocked", name = "Unlock Shield", description = "Unlock shield slot")
	default boolean shieldUnlocked()
	{
		return false;
	}

	@ConfigItem(keyName = "legsUnlocked", name = "Unlock Legs", description = "Unlock legs slot")
	default boolean legsUnlocked()
	{
		return false;
	}

	@ConfigItem(keyName = "glovesUnlocked", name = "Unlock Gloves", description = "Unlock gloves slot")
	default boolean glovesUnlocked()
	{
		return false;
	}

	@ConfigItem(keyName = "bootsUnlocked", name = "Unlock Boots", description = "Unlock boots slot")
	default boolean bootsUnlocked()
	{
		return false;
	}

	@ConfigItem(keyName = "ringUnlocked", name = "Unlock Ring", description = "Unlock ring slot")
	default boolean ringUnlocked()
	{
		return false;
	}

	@ConfigItem(keyName = "ammoUnlocked", name = "Unlock Ammo", description = "Unlock ammo slot")
	default boolean ammoUnlocked()
	{
		return false;
	}
}
