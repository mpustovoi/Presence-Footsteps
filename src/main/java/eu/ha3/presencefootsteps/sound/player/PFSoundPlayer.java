package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.world.Association;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.BlockSoundGroup;

public class PFSoundPlayer implements SoundPlayer, StepSoundPlayer {
    private final ImmediateSoundPlayer immediatePlayer;
    private final DelayedSoundPlayer delayedPlayer;

    private final SoundEngine engine;

    public PFSoundPlayer(SoundEngine engine) {
        this.engine = engine;
        immediatePlayer = new ImmediateSoundPlayer(engine);
        delayedPlayer = new DelayedSoundPlayer(this);
    }

    @Override
    public void think() {
        delayedPlayer.think();
    }

    @Override
    public Random getRNG() {
        return immediatePlayer.getRNG();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean playStep(Association assos, State eventType, Options options) {
        if (!assos.isResult()) {
            return false;
        }

        if (!assos.isNotEmitter()) {
            if (assos.hasAssociation()) {
                engine.getIsolator().acoustics().playAcoustic(assos, eventType, options);
            } else if (!assos.state().isLiquid()) {
                BlockSoundGroup soundType = assos.soundGroup();
                BlockState above = assos.source().getWorld().getBlockState(assos.pos().up());

                if (above.isOf(Blocks.SNOW)) {
                    soundType = above.getSoundGroup();
                }

                immediatePlayer.playSound(assos.source(), soundType.getStepSound().getId().toString(), soundType.getVolume() * 0.15F, soundType.getPitch(), options);
            }
        }

        return true;
    }

    @Override
    public void playSound(LivingEntity location, String soundName, float volume, float pitch, Options options) {
        if (options.containsKey("delay_min") && options.containsKey("delay_max")) {
            delayedPlayer.playSound(location, soundName, volume, pitch, options);
            return;
        }
        immediatePlayer.playSound(location, soundName, volume, pitch, options);
    }
}
