package com.blakebr0.ironjetpacks.item;

import com.blakebr0.ironjetpacks.registry.Jetpack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

public class ComponentItem extends Item implements Colored, Enableable {
    private final String name;
    private final String type;
    private final boolean enabled;
    private final int color;
    
    public ComponentItem(Jetpack jetpack, String type, Item.Properties settings) {
        super(settings.rarity(jetpack.rarity));
        this.name = jetpack.name;
        this.color = jetpack.color;
        this.enabled = !jetpack.disabled;
        this.type = type;
    }
    
    @Override
    public Component getName(ItemStack stack) {
        String name = StringUtils.capitalize(this.name.replace(" ", "_"));
        return Component.translatable("item.iron-jetpacks." + this.type, name);
    }
    
    @Override
    public int getColorTint(int i) {
        return this.color;
    }
    
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
