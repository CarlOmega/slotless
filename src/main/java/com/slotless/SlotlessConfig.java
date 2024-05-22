package com.slotless;

import net.runelite.api.ItemID;
import net.runelite.client.config.*;

@ConfigGroup(SlotlessConfig.GROUP)
public interface SlotlessConfig extends Config {
    String GROUP = "slotless";

    @ConfigSection(
            name = "Inventory Filler",
            description = "Inventory filler settings",
            position = 0
    )
    String inventorySection = "inventory";

    @ConfigSection(
            name = "Equipment Unlocks",
            description = "Equipment unlocks for slotless",
            position = 1
    )
    String equipmentSection = "equipment";


    @ConfigItem(
            keyName = "displayInfobox",
            name = "Display Infobox",
            description = "Shows information about unlocks",
            position = 99
    )
    default boolean displayInfobox() {
        return true;
    }

    @Range(
            max = 28,
            min = 1
    )
    @ConfigItem(
            keyName = "inventorySlotUnlockCount",
            name = "Slots Unlocked",
            description = "Amount of inventory slots unlocked",
            section = inventorySection,
            position = 0
    )
    default int inventorySlotUnlockCount() {
        return 1;
    }

    @ConfigItem(
            keyName = "inventoryReplacementItemId",
            name = "Inventory Item ID",
            description = "Item to be replaced with Bank Filler",
            section = inventorySection
    )
    default int inventoryReplacementItemId() {
        return ItemID.DOOR_KEY;
    }

    @ConfigItem(
            keyName = "inventoryFillerEnabled",
            name = "Enable Inventory Filler",
            description = "Enable Filler for inventory",
            section = inventorySection
    )
    default boolean inventoryFillerEnabled() {
        return true;
    }

    @ConfigItem(keyName = "headUnlocked", name = "Unlock Head", description = "Unlock head slot", section = equipmentSection)
    default boolean headUnlocked() {
        return false;
    }

    @ConfigItem(keyName = "capeUnlocked", name = "Unlock Cape", description = "Unlock cape slot", section = equipmentSection)
    default boolean capeUnlocked() {
        return false;
    }

    @ConfigItem(keyName = "amuletUnlocked", name = "Unlock Amulet", description = "Unlock amulet slot", section = equipmentSection)
    default boolean amuletUnlocked() {
        return false;
    }

    @ConfigItem(keyName = "weaponUnlocked", name = "Unlock Weapon", description = "Unlock weapon slot", section = equipmentSection)
    default boolean weaponUnlocked() {
        return false;
    }

    @ConfigItem(keyName = "bodyUnlocked", name = "Unlock Body", description = "Unlock body slot", section = equipmentSection)
    default boolean bodyUnlocked() {
        return false;
    }

    @ConfigItem(keyName = "shieldUnlocked", name = "Unlock Shield", description = "Unlock shield slot", section = equipmentSection)
    default boolean shieldUnlocked() {
        return false;
    }

    @ConfigItem(keyName = "legsUnlocked", name = "Unlock Legs", description = "Unlock legs slot", section = equipmentSection)
    default boolean legsUnlocked() {
        return false;
    }

    @ConfigItem(keyName = "glovesUnlocked", name = "Unlock Gloves", description = "Unlock gloves slot", section = equipmentSection)
    default boolean glovesUnlocked() {
        return false;
    }

    @ConfigItem(keyName = "bootsUnlocked", name = "Unlock Boots", description = "Unlock boots slot", section = equipmentSection)
    default boolean bootsUnlocked() {
        return false;
    }

    @ConfigItem(keyName = "ringUnlocked", name = "Unlock Ring", description = "Unlock ring slot", section = equipmentSection)
    default boolean ringUnlocked() {
        return false;
    }

    @ConfigItem(keyName = "ammunitionUnlocked", name = "Unlock Ammo", description = "Unlock ammo slot", section = equipmentSection)
    default boolean ammoUnlocked() {
        return false;
    }
}
