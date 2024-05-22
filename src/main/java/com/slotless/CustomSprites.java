package com.slotless;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.SpriteOverride;

@RequiredArgsConstructor
public enum CustomSprites implements SpriteOverride {
    SLOTLESS_MODE(-112308, "slotless_mode.png");

    @Getter
    private final int spriteId;

    @Getter
    private final String fileName;
}
