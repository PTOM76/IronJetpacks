package com.blakebr0.ironjetpacks.handler;

import com.blakebr0.ironjetpacks.config.ModConfigs;
import com.blakebr0.ironjetpacks.item.JetpackItem;
import com.blakebr0.ironjetpacks.registry.Jetpack;
import com.blakebr0.ironjetpacks.sound.JetpackSound;
import com.blakebr0.ironjetpacks.util.JetpackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class JetpackClientHandler {
    private static final RandomSource RANDOM = RandomSource.create();
    
    public static void onClientTick(Minecraft client) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            if (!mc.isPaused()) {
                ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
                Item item = chest.getItem();
                if (!chest.isEmpty() && item instanceof JetpackItem && JetpackUtils.isFlying(mc.player)) {
                    if (ModConfigs.getClient().general.enableJetpackParticles && (mc.options.particles().get() != ParticleStatus.MINIMAL)) {
                        Jetpack jetpack = ((JetpackItem) item).getJetpack();
                        Vec3 playerPos = mc.player.position().add(0, 1.5, 0);
                        
                        float random = (RANDOM.nextFloat() - 0.5F) * 0.1F;
                        double[] sneakBonus = mc.player.isShiftKeyDown() ? new double[]{-0.30, -0.10} : new double[]{0, 0};
                        
                        Vec3 rotation = Vec3.directionFromRotation(0, mc.player.yBodyRot);
                        Vec3 vLeft = new Vec3(-0.18, -0.90 + sneakBonus[1], -0.30 + sneakBonus[0]).xRot(0).yRot(mc.player.yBodyRot * -0.017453292F);
                        Vec3 vRight = new Vec3(0.18, -0.90 + sneakBonus[1], -0.30 + sneakBonus[0]).xRot(0).yRot(mc.player.yBodyRot * -0.017453292F);
                        
                        Vec3 v = playerPos.add(vLeft).add(mc.player.getDeltaMovement().scale(jetpack.speedSide));
                        mc.particleEngine.createParticle(ParticleTypes.FLAME, v.x, v.y, v.z, random, -0.2D, random);
                        mc.particleEngine.createParticle(ParticleTypes.SMOKE, v.x, v.y, v.z, random, -0.2D, random);
                        
                        v = playerPos.add(vRight).add(mc.player.getDeltaMovement().scale(jetpack.speedSide));
                        mc.particleEngine.createParticle(ParticleTypes.FLAME, v.x, v.y, v.z, random, -0.2D, random);
                        mc.particleEngine.createParticle(ParticleTypes.SMOKE, v.x, v.y, v.z, random, -0.2D, random);
                    }
                    
                    if (ModConfigs.getClient().general.enableJetpackSounds && !JetpackSound.playing(mc.player.getId())) {
                        mc.getSoundManager().play(new JetpackSound(mc.player, RANDOM));
                    }
                }
            }
        }
    }
}
