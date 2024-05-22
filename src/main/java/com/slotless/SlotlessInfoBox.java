package com.slotless;

import net.runelite.client.ui.overlay.infobox.Counter;

import java.awt.image.BufferedImage;

public class SlotlessInfoBox extends Counter {
    private final SlotlessPlugin plugin;
    private final SlotlessConfig config;

    SlotlessInfoBox(BufferedImage image, SlotlessPlugin plugin, SlotlessConfig config) {
        super(image, plugin, plugin.getSpendableSlots());
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public String getText() {
        return String.valueOf(plugin.getSpendableSlots());
    }

    @Override
    public String getTooltip() {
        return "Slots used: " + plugin.getCurrentSlotCount() + "/" + plugin.getMaxSlotsUnlockedCount() + "</br>"
                + "QP to next Slot: " + plugin.getQuestPointstoNext();
    }

    @Override
    public boolean render() {
        return super.render() && config.displayInfobox();
    }
}
