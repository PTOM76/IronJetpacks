package com.blakebr0.ironjetpacks.sound;

import com.blakebr0.ironjetpacks.util.JetpackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class JetpackSound extends AbstractTickableSoundInstance {
    private static final Map<Integer, JetpackSound> PLAYING_FOR = Collections.synchronizedMap(new HashMap<>());
    private final Player player;
    
    public JetpackSound(Player player, RandomSource random) {
        super(ModSounds.JETPACK.get(), SoundSource.PLAYERS, random);
        this.player = player;
        this.looping = true;
        PLAYING_FOR.put(player.getId(), this);
    }
    
    public static boolean playing(int entityId) {
        return PLAYING_FOR.containsKey(entityId) && PLAYING_FOR.get(entityId) != null && !PLAYING_FOR.get(entityId).isStopped();
    }
    
    @Override
    public void tick() {
        BlockPos pos = this.player.blockPosition();
        this.x = (float) pos.getX();
        this.y = (float) pos.getY() - 10;
        this.z = (float) pos.getZ();
        
        if (!JetpackUtils.isFlying(this.player)) {
            synchronized (PLAYING_FOR) {
                PLAYING_FOR.remove(this.player.getId());
                stop();
            }
        }
    }
}
