package com.slotless;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;
import org.apache.commons.lang3.StringUtils;

import java.awt.image.BufferedImage;
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
    private ConfigManager configManager;
    @Inject
    private ChatMessageManager chatMessageManager;
    @Inject
    private ItemManager itemManager;
    @Inject
    private SpriteManager spriteManager;
    @Inject
    private InfoBoxManager infoBoxManager;
    private int slotlessIconOffset = -1;
    private BufferedImage slotlessImage;
    private Widget invUpdateWidget;

    @Override
    protected void startUp() {
        spriteManager.addSpriteOverrides(CustomSprites.values());
        load();
        infoBoxManager.addInfoBox(new SlotlessInfoBox(slotlessImage, this, config));
        clientThread.invoke(this::redrawInventory);

        clientThread.invoke(() ->
        {
            if (client.getGameState() == GameState.LOGGED_IN) {
                setChatboxName();
            }
        });
    }

    @Override
    protected void shutDown() {
        clientThread.invoke(this::redrawInventory);
        clientThread.invoke(this::cleanupEquipment);
        spriteManager.removeSpriteOverrides(CustomSprites.values());
        infoBoxManager.removeIf(SlotlessInfoBox.class::isInstance);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (client.getGameState() != GameState.LOADING && client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        String name = Text.removeTags(chatMessage.getName());
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        switch (chatMessage.getType()) {
            case PRIVATECHAT:
            case MODPRIVATECHAT:
            case FRIENDSCHAT:
            case PUBLICCHAT:
            case MODCHAT:
                if (name.equals(player.getName())) {
                    final MessageNode messageNode = chatMessage.getMessageNode();
                    messageNode.setName("<img=" + slotlessIconOffset + ">" + player.getName());
                    client.refreshChat();
                }
                break;
        }
    }

    @Subscribe
    public void onScriptCallbackEvent(ScriptCallbackEvent scriptCallbackEvent) {
        if (scriptCallbackEvent.getEventName().equals("setChatboxInput")) {
            setChatboxName();
        }
    }

    private void setChatboxName() {
        Widget chatboxInput = client.getWidget(ComponentID.CHATBOX_INPUT);
        Player player = client.getLocalPlayer();
        if (player != null && chatboxInput != null) {
            String text = chatboxInput.getText();
            int idx = text.indexOf(':');
            if (idx != -1) {
                String newText = "<img=" + slotlessIconOffset + ">" + player.getName() + text.substring(idx);
                chatboxInput.setText(newText);
            }
        }
    }

    private void cleanupEquipment() {
        // Force redraw and reset sprites since there is very little that updates the worn equipment
        replaceEquipment(client.getWidget(ComponentID.EQUIPMENT_INVENTORY_ITEM_CONTAINER), true);
    }

    private void redrawInventory() {
        client.runScript(914, -2147483644, 1130, 4);
        client.runScript(3281, 2776, 1);
        client.runScript(Objects.requireNonNull(client.getWidget(ComponentID.INVENTORY_CONTAINER)).getOnInvTransmitListener());
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

    int getMaxSlotsUnlockedCount() {
        int questPoints = client.getVarpValue(VarPlayer.QUEST_POINTS);
        return 1 + questPoints / 10;
    }

    int getCurrentSlotCount() {
        int total = config.inventorySlotUnlockCount();

        total += config.headUnlocked() ? 1 : 0;
        total += config.capeUnlocked() ? 1 : 0;
        total += config.amuletUnlocked() ? 1 : 0;
        total += config.weaponUnlocked() ? 1 : 0;
        total += config.bodyUnlocked() ? 1 : 0;
        total += config.shieldUnlocked() ? 1 : 0;
        total += config.legsUnlocked() ? 1 : 0;
        total += config.glovesUnlocked() ? 1 : 0;
        total += config.bootsUnlocked() ? 1 : 0;
        total += config.ringUnlocked() ? 1 : 0;
        total += config.ammoUnlocked() ? 1 : 0;

        return total;
    }

    int getSpendableSlots() {
        return getMaxSlotsUnlockedCount() - getCurrentSlotCount();
    }

    int getQuestPointstoNext() {
        int questPoints = client.getVarpValue(VarPlayer.QUEST_POINTS);
        return 10 - questPoints % 10;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals(SlotlessConfig.GROUP)) {
            String key = event.getKey();
            if (getSpendableSlots() < 0) {
                configManager.setConfiguration(SlotlessConfig.GROUP, event.getKey(), event.getOldValue());
                clientThread.invoke(this::warningNotEnoughSlots);
                return;
            }
            if (key.equals("inventorySlotUnlockCount")) {
                if (event.getOldValue() != null && config.inventorySlotUnlockCount() > Integer.parseInt(event.getOldValue())) {
                    clientThread.invoke(() -> openPopUp("Slot Unlocked", String.format("New slot:<br><br><col=ffffff>%s Inventory Slots Unlocked</col>", config.inventorySlotUnlockCount())));
                }
            } else if (key.endsWith("Unlocked") && event.getNewValue() != null && event.getNewValue().equals(Boolean.TRUE.toString())) {
                clientThread.invoke(() -> openPopUp("Equipment Unlocked", String.format("New slot:<br><br><col=ffffff>%s</col>", StringUtils.capitalize(key.substring(0, key.length() - 8)))));
            }
            clientThread.invoke(this::redrawInventory);
        }
    }

    private void warningNotEnoughSlots() {
        final String message = new ChatMessageBuilder()
                .append("WARNING! You do not have enough slots to spend.")
                .build();

        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(message)
                .build());
    }

    private void load() {
        final IndexedSprite[] modIcons = client.getModIcons();

        if (slotlessIconOffset != -1 || modIcons == null) {
            return;
        }

        BufferedImage image = ImageUtil.loadImageResource(getClass(), "slotless_mode_chat_icon.png");
        slotlessImage = ImageUtil.loadImageResource(getClass(), "slotless_mode.png");
        IndexedSprite indexedSprite = ImageUtil.getImageIndexedSprite(image, client);

        slotlessIconOffset = modIcons.length;

        final IndexedSprite[] newModIcons = Arrays.copyOf(modIcons, modIcons.length + 1);
        newModIcons[newModIcons.length - 1] = indexedSprite;

        client.setModIcons(newModIcons);
    }


    private void openPopUp(String title, String description) {
        WidgetNode widgetNode = client.openInterface((161 << 16) | 13, 660, WidgetModalMode.MODAL_CLICKTHROUGH);
        client.runScript(3343, title, description, -1);

        clientThread.invokeLater(() -> {
            Widget w = client.getWidget(660, 1);
            if (w == null || w.getWidth() > 0) {
                return false;
            }

            client.closeInterface(widgetNode, true);
            return true;
        });
    }

    private void replaceInventory(Widget w) {
        int fillerId = config.inventoryReplacementItemId();
        int maxFillerAmount = 28 - config.inventorySlotUnlockCount();
        if (w == null || !config.inventoryFillerEnabled()) {
            return;
        }
        Widget[] children = w.getDynamicChildren();
        int fillerCount = 0;
        for (Widget i : children) {
            if (i.getItemId() == fillerId) {
                fillerCount++;
            }
        }
        int diff = fillerCount - maxFillerAmount;

        for (Widget i : children) {
            if (i.getItemId() == fillerId) {
                if (diff > 0) {
                    diff--;
                } else {
                    i.setName("Filler");
                    i.setTargetVerb(null);
                    i.setItemId(ItemID.BANK_FILLER);
                    i.setClickMask(0);
                    i.setOnDragCompleteListener((Object[]) null);
                    i.setOnDragListener((Object[]) null);
                    Arrays.fill(Objects.requireNonNull(i.getActions()), "");
                }
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
                if (child != null) {
                    if (isSlotLocked(slot) && !reset) {
                        child.setSpriteId(CustomSprites.SLOTLESS_MODE.getSpriteId());
                        child.setSize(28, 28);
                    } else {
                        child.setSpriteId(SLOT_SPRITE_IDS.get(slot));
                        child.setSize(32, 32);
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
            if ("Wear".equalsIgnoreCase(actions[actionIdx]) ||
                    "Wield".equalsIgnoreCase(actions[actionIdx]) ||
                    "Equip".equalsIgnoreCase(actions[actionIdx]) ||
                    "Hold".equalsIgnoreCase(actions[actionIdx])) {
                actions[actionIdx] = "";
            }
        }
    }
}
