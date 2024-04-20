package com.slotless;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@PluginDescriptor(name = "Slotless")
public class SlotlessPlugin extends Plugin {
    private static final List<Integer> SLOT_SPRITE_IDS = ImmutableList.<Integer>builder()
            .add(SpriteID.EQUIPMENT_SLOT_HEAD)
            .add(SpriteID.EQUIPMENT_SLOT_CAPE)
            .add(SpriteID.EQUIPMENT_SLOT_NECK)
            .add(SpriteID.EQUIPMENT_SLOT_WEAPON)
            .add(SpriteID.EQUIPMENT_SLOT_TORSO)
            .add(SpriteID.EQUIPMENT_SLOT_SHIELD)
            .add(SpriteID.EQUIPMENT_SLOT_LEGS)
            .add(SpriteID.EQUIPMENT_SLOT_HANDS)
            .add(SpriteID.EQUIPMENT_SLOT_FEET)
            .add(SpriteID.EQUIPMENT_SLOT_RING)
            .add(SpriteID.EQUIPMENT_SLOT_AMMUNITION).build();
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private SlotlessConfig config;
    @Inject
    private ItemManager itemManager;
    @Inject
    private SpriteManager spriteManager;
    private Widget invUpdateWidget;

    @Override
    protected void startUp() {
        spriteManager.addSpriteOverrides(CustomSprites.values());
        clientThread.invoke(this::redrawInventory);
    }

    @Override
    protected void shutDown() {
        clientThread.invoke(this::redrawInventory);
        clientThread.invoke(this::cleanupEquipment);
        spriteManager.removeSpriteOverrides(CustomSprites.values());
    }

    private void cleanupEquipment() {
        // Force redraw and reset sprites since there is very little that updates the worn equipment
        replaceEquipment(client.getWidget(ComponentID.EQUIPMENT_INVENTORY_ITEM_CONTAINER), true);
    }

    private void redrawInventory() {
        client.runScript(Objects.requireNonNull(client.getWidget(ComponentID.INVENTORY_CONTAINER)).getOnInvTransmitListener());
        client.runScript(914, -2147483644, 1130, 4);
        client.runScript(3281, 2776, 1);
    }


    @Subscribe
    public void onScriptPreFired(ScriptPreFired scriptPreFired) {
        // [proc,interface_inv_update_big]
        if (scriptPreFired.getScriptId() == 153) {
            // [proc,interface_inv_update_big](component $component0, inv $inv1, int $int2, int $int3, int $int4, component $component5, string $string0, string $string1, string $string2, string $string3, string $string4, string $string5, string $string6, string $string7, string $string8)
            int w = client.getIntStack()[client.getIntStackSize() - 6]; // first argument
            invUpdateWidget = client.getWidget(w);
        }
        // [proc,deathkeep_left_setsection]
        else if (scriptPreFired.getScriptId() == 975) {
            // [proc,deathkeep_left_setsection](string $text0, component $component0, int $comsubid1, int $int2, int $int3, int $int4)
            int w = client.getIntStack()[client.getIntStackSize() - 5]; // second argument
            invUpdateWidget = client.getWidget(w);
        }
    }

    @Provides
    SlotlessConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SlotlessConfig.class);
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired scriptPostFired) {
        final int id = scriptPostFired.getScriptId();
        // [proc,inventory_build]
        if (id == ScriptID.INVENTORY_DRAWITEM - 1) {
            replaceInventory(client.getWidget(ComponentID.INVENTORY_CONTAINER));
        }
        // [proc,ge_pricechecker_redraw]
        else if (id == 787) {
            replaceInventory(client.getWidget(ComponentID.GUIDE_PRICES_INVENTORY_ITEM_CONTAINER));
        }
        // [proc,interface_inv_update_big]
        // [proc,deathkeep_left_redraw]
        else if (id == 153 || id == 975) {
            if (invUpdateWidget != null) {
                replaceInventory(invUpdateWidget);
                invUpdateWidget = null;
            }
        }
        // [proc,bankside_build]
        else if (id == 296) {
            replaceInventory(client.getWidget(ComponentID.BANK_INVENTORY_ITEM_CONTAINER));
        }
        // [proc,equipment_build]
        else if (id == 914) {
            replaceEquipment(client.getWidget(ComponentID.EQUIPMENT_INVENTORY_ITEM_CONTAINER), false);
        }
        // [proc,equip_your_character]
        else if (id == 3281) {
            replaceInventory(client.getWidget(ComponentID.INVENTORY_CONTAINER));
            replaceEquipment(client.getWidget(ComponentID.EQUIPMENT_BONUSES_PARENT), false);
        }
        // [proc,shop_interface]
        else if (id == 1074) {
            replaceInventory(client.getWidget(ComponentID.INVENTORY_CONTAINER));
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals(SlotlessConfig.GROUP)) {
            clientThread.invoke(this::redrawInventory);
        }
    }

    private void replaceInventory(Widget w) {
        int filler = config.inventoryReplacementItemId();
        if (w == null || !config.inventoryFillerEnabled()) {
            return;
        }
        for (Widget i : w.getDynamicChildren()) {
            if (i.getItemId() == filler) {
                i.setName("Filler");
                i.setTargetVerb(null);
                i.setItemId(ItemID.BANK_FILLER);
                i.setClickMask(0);
                i.setOnDragCompleteListener((Object[]) null);
                i.setOnDragListener((Object[]) null);
                Arrays.fill(Objects.requireNonNull(i.getActions()), "");
            } else if (i.getActions() != null) {
                final ItemStats itemStats = itemManager.getItemStats(i.getItemId(), false);
                if (itemStats != null && itemStats.isEquipable()) {
                    final ItemEquipmentStats equipmentStats = itemStats.getEquipment();
                    final int slot = mapItemStatSlotToEquipment(equipmentStats.getSlot());
                    if (slot > -1 && isSlotLocked(slot) ||
                            (equipmentStats.isTwoHanded() && !(config.weaponUnlocked() && config.shieldUnlocked()))
                    ) {
                        removeEquipOption(i);
                    }

                }
            }
        }
    }

    private void replaceEquipment(Widget w, boolean reset) {
        if (w == null || w.getId() == -1) {
            return;
        }
        final int equipmentOffset = w.getId() == ComponentID.EQUIPMENT_INVENTORY_ITEM_CONTAINER ? 15 : 9;
        Widget[] slots = w.getStaticChildren();
        for (Widget slotWidget : slots) {
            final int slotWidgetIdx = slotWidget.getId() - w.getId();
            final int slot = slotWidgetIdx - equipmentOffset;
            if (slot >= 0 && slot < 11) {
                Widget child = slotWidget.getChild(2);
                log.debug("Equipment Slotless test {}, {}, {}", slotWidget.getId(), slots.length, w.getId());
                if (child != null) {
                    if (isSlotLocked(slot) && !reset) {
                        child.setSpriteId(CustomSprites.SLOTLESS_MODE.getSpriteId());
                    } else {
                        child.setSpriteId(SLOT_SPRITE_IDS.get(slot));
                    }
                    child.revalidate();
                }
            }
        }
    }

    private int mapItemStatSlotToEquipment(int slot) {
        // No idea why there is inconsistency here but will look into it later
        // from https://static.runelite.net/item/stats.ids.min.json and looks like it just skips those numbers.
        switch (slot) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return slot;
            case 7:
                return 6;
            case 9:
                return 7;
            case 10:
                return 8;
            case 12:
                return 9;
            case 13:
                return 10;
            default:
                return -1;
        }
    }


    private boolean isSlotLocked(int equipmentSlot) {
        EquipmentInventorySlot whichSlot = EquipmentInventorySlot.values()[equipmentSlot];
        switch (whichSlot) {
            case HEAD:
                return !config.headUnlocked();
            case CAPE:
                return !config.capeUnlocked();
            case AMULET:
                return !config.amuletUnlocked();
            case WEAPON:
                return !config.weaponUnlocked();
            case BODY:
                return !config.bodyUnlocked();
            case SHIELD:
                return !config.shieldUnlocked();
            case LEGS:
                return !config.legsUnlocked();
            case GLOVES:
                return !config.glovesUnlocked();
            case BOOTS:
                return !config.bootsUnlocked();
            case RING:
                return !config.ringUnlocked();
            case AMMO:
                return !config.ammoUnlocked();
            default:
                return true;
        }
    }

    private void removeEquipOption(Widget i) {
        if (i.getActions() == null) {
            return;
        }
        String[] actions = i.getActions();
        for (int actionIdx = 0; actionIdx < actions.length; ++actionIdx) {
            if ("Wear".equalsIgnoreCase(actions[actionIdx]) || "Wield".equalsIgnoreCase(actions[actionIdx]) || "Equip".equalsIgnoreCase(actions[actionIdx])) {
                actions[actionIdx] = "";
            }
        }
    }
}
