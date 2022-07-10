package com.blakebr0.ironjetpacks.item;

import com.blakebr0.ironjetpacks.config.ModConfigs;
import com.blakebr0.ironjetpacks.handler.InputHandler;
import com.blakebr0.ironjetpacks.item.storage.StackBaseStorage;
import com.blakebr0.ironjetpacks.lib.ModTooltips;
import com.blakebr0.ironjetpacks.mixins.ServerPlayNetworkHandlerAccessor;
import com.blakebr0.ironjetpacks.registry.Jetpack;
import com.blakebr0.ironjetpacks.util.JetpackUtils;
import com.blakebr0.ironjetpacks.util.UnitUtils;
import dev.architectury.extensions.ItemExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public class JetpackItem extends DyeableArmorItem implements Colored, DyeableLeatherItem, Enableable, ItemExtension {
    private final Jetpack jetpack;
    
    public JetpackItem(Jetpack jetpack, Properties settings) {
        super(JetpackUtils.makeArmorMaterial(jetpack), EquipmentSlot.CHEST, settings.durability(0).rarity(jetpack.rarity));
        this.jetpack = jetpack;
    }
    
    @Override
    public Component getName(ItemStack stack) {
        String name = StringUtils.capitalize(this.jetpack.name.replace(" ", "_"));
        return Component.translatable("item.iron-jetpacks.jetpack", name);
    }
    
    /*
     * Jetpack logic is very much like Simply Jetpacks, since I used it to learn how to make this work
     * Credit to Tonius & Tomson124
     * https://github.com/Tomson124/SimplyJetpacks-2/blob/1.12/src/main/java/tonius/simplyjetpacks/item/rewrite/ItemJetpack.java
     */
    @Override
    public void tickArmor(ItemStack stack, Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        Item item = chest.getItem();
        if (!chest.isEmpty() && item instanceof JetpackItem) {
            JetpackItem jetpack = (JetpackItem) item;
            if (jetpack.isEngineOn(chest)) {
                boolean hover = jetpack.isHovering(chest);
                if (InputHandler.isHoldingUp(player) || hover && !player.isOnGround()) {
                    Jetpack info = jetpack.getJetpack();
                    
                    double hoverSpeed = InputHandler.isHoldingDown(player) ? info.speedHover : info.speedHoverSlow;
                    double currentAccel = info.accelVert * (player.getDeltaMovement().y() < 0.3D ? 2.5D : 1.0D);
                    double currentSpeedVertical = info.speedVert * (player.isUnderWater() ? 0.4D : 1.0D);
                    
                    double usage = player.isSprinting() ? info.usage * info.sprintFuel : info.usage;
                    
                    boolean creative = info.creative;
                    StackBaseStorage storage = new StackBaseStorage(stack.copy());
    
                    EnergyStorage energy = EnergyStorage.ITEM.find(stack.copy(), ContainerItemContext.ofPlayerSlot(player, storage));
                    
                    try (Transaction transaction = Transaction.openOuter()) {
                        if (creative || energy.extract((long) usage, transaction) >= usage) {
                            if (!creative) {
                                transaction.commit();
                                ItemStack newStack = storage.getResource().toStack();
                                stack.setTag(newStack.getTag());
                                stack.setCount(newStack.getCount());
                            }
    
                            double motionY = player.getDeltaMovement().y();
                            if (InputHandler.isHoldingUp(player)) {
                                if (!hover) {
                                    this.fly(player, Math.min(motionY + currentAccel, currentSpeedVertical));
                                } else {
                                    if (InputHandler.isHoldingDown(player)) {
                                        this.fly(player, Math.min(motionY + currentAccel, -info.speedHoverSlow));
                                    } else {
                                        this.fly(player, Math.min(motionY + currentAccel, info.speedHover));
                                    }
                                }
                            } else {
                                this.fly(player, Math.min(motionY + currentAccel, -hoverSpeed));
                            }
    
                            float speedSideways = (float) (player.isShiftKeyDown() ? info.speedSide * 0.5F : info.speedSide);
                            float speedForward = (float) (player.isSprinting() ? speedSideways * info.sprintSpeed : speedSideways);
    
                            if (InputHandler.isHoldingForwards(player)) {
                                player.moveRelative(1, new Vec3(0, 0, speedForward));
                            }
    
                            if (InputHandler.isHoldingBackwards(player)) {
                                player.moveRelative(1, new Vec3(0, 0, -speedSideways * 0.8F));
                            }
    
                            if (InputHandler.isHoldingLeft(player)) {
                                player.moveRelative(1, new Vec3(speedSideways, 0, 0));
                            }
    
                            if (InputHandler.isHoldingRight(player)) {
                                player.moveRelative(1, new Vec3(-speedSideways, 0, 0));
                            }
    
                            if (!player.level.isClientSide()) {
                                player.fallDistance = 0.0F;
        
                                if (player instanceof ServerPlayer) {
                                    ((ServerPlayNetworkHandlerAccessor) ((ServerPlayer) player).connection).setFloatingTicks(0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return ModConfigs.get().general.enchantableJetpacks && this.jetpack.enchantablilty > 0;
    }
    
    @Override
    public int getBarWidth(ItemStack stack) {
        EnergyStorage energy = EnergyStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack));
        double stored = energy.getCapacity() - energy.getAmount();
        return (int) Math.round(13.0F - (stored / energy.getCapacity()) * 13.0F);
    }
    
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !this.jetpack.creative;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced) {
        if (!this.jetpack.creative) {
            EnergyStorage energy = EnergyStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack));
            tooltip.add(Component.literal(UnitUtils.formatEnergy(energy.getAmount(), null)).withStyle(ChatFormatting.GRAY).append(" / ").append(Component.literal(UnitUtils.formatEnergy(jetpack.capacity, null))));
        } else {
            tooltip.add(Component.literal("-1 E / ").withStyle(ChatFormatting.GRAY).append(ModTooltips.INFINITE.color(ChatFormatting.GRAY)).append(" E"));
        }
        
        Component tier = ModTooltips.TIER.args(this.jetpack.creative ? "Creative" : this.jetpack.tier).withStyle(this.jetpack.rarity.color);
        Component engine = ModTooltips.ENGINE.color(isEngineOn(stack) ? ChatFormatting.GREEN : ChatFormatting.RED);
        Component hover = ModTooltips.HOVER.color(isHovering(stack) ? ChatFormatting.GREEN : ChatFormatting.RED);
        
        tooltip.add(ModTooltips.STATE_TOOLTIP_LAYOUT.args(tier, engine, hover));
        
        if (ModConfigs.getClient().general.enableAdvancedInfoTooltips) {
            tooltip.add(Component.literal(""));
            if (!Screen.hasShiftDown()) {
                tooltip.add(Component.translatable("tooltip.iron-jetpacks.hold_shift_for_info"));
            } else {
                tooltip.add(ModTooltips.FUEL_USAGE.args(this.jetpack.usage + " E/t"));
                tooltip.add(ModTooltips.VERTICAL_SPEED.args(this.jetpack.speedVert));
                tooltip.add(ModTooltips.VERTICAL_ACCELERATION.args(this.jetpack.accelVert));
                tooltip.add(ModTooltips.HORIZONTAL_SPEED.args(this.jetpack.speedSide));
                tooltip.add(ModTooltips.HOVER_SPEED.args(this.jetpack.speedHoverSlow));
                tooltip.add(ModTooltips.DESCEND_SPEED.args(this.jetpack.speedHover));
                tooltip.add(ModTooltips.SPRINT_MODIFIER.args(this.jetpack.sprintSpeed));
                tooltip.add(ModTooltips.SPRINT_FUEL_MODIFIER.args(this.jetpack.sprintFuel));
            }
        }
    }
    
    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks) {
        if (this.allowedIn(group)) {
            stacks.add(new ItemStack(this));
            
            if (!jetpack.creative) {
                ItemStack stack = new ItemStack(this);
                stack.getOrCreateTag().putDouble("energy", jetpack.capacity);
                stacks.add(stack);
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public int getColorTint(int i) {
        return i == 1 ? this.jetpack.color : -1;
    }
    
    @Override
    public boolean hasCustomColor(ItemStack stack) {
        return true;
    }
    
    @Override
    public int getColor(ItemStack stack) {
        return this.jetpack.color;
    }
    
    @Override
    public void clearColor(ItemStack stack) {
        
    }
    
    @Override
    public void setColor(ItemStack stack, int color) {
        
    }
    
    @Override
    public boolean isEnabled() {
        return !this.jetpack.disabled;
    }
    
    public Jetpack getJetpack() {
        return this.jetpack;
    }
    
    // No output
    public double getMaxOutput() {
        return 0;
    }
    
    public double getMaxInput() {
        return jetpack.capacity / 20.0;
    }
    
    public boolean isEngineOn(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("Engine") && tag.getBoolean("Engine");
    }
    
    public boolean toggleEngine(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean current = tag.contains("Engine") && tag.getBoolean("Engine");
        tag.putBoolean("Engine", !current);
        return !current;
    }
    
    public boolean isHovering(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("Hover") && tag.getBoolean("Hover");
    }
    
    public boolean toggleHover(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean current = tag.contains("Hover") && tag.getBoolean("Hover");
        tag.putBoolean("Hover", !current);
        return !current;
    }
    
    private void fly(Player player, double y) {
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x(), y, motion.z());
    }
}
