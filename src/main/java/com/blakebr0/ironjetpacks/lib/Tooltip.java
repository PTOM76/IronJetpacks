package com.blakebr0.ironjetpacks.lib;

import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class Tooltip {
    private final String translationKey;
    
    public Tooltip(String translationKey) {
        this.translationKey = Objects.requireNonNull(translationKey);
    }
    
    public MutableComponent color(ChatFormatting formatting) {
        return Component.translatable(translationKey).withStyle(formatting);
    }
    
    public MutableComponent args(Object... args) {
        return Component.translatable(translationKey, args);
    }
    
    public String asFormattedString() {
        return Component.translatable(translationKey).getString();
    }
    
    public MutableComponent build() {
        return Component.translatable(translationKey);
    }
}
